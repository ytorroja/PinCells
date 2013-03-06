package art.petaka.pincells;

import processing.core.*;

public class Pin {
	PApplet _parent;
	
	// Drawing variables
	// All drawing dimmensions are normalized to 0-1 (wrt size of space or screen)
	float _x, _y;				// Drawing x, y position
	float _r;					// Drawing size
	
	// Communication variables
	// All times are expressed in ms

	// Physical layer
	float 	_beta;				// Emitting angle
	float 	_power;				// Emitting power
	float 	_alfa;				// Facing angle

	// Medium Access Control Layer
	int   	_talkTime;			// Time needed for a message to be sent
	int   	_receivedTime;		// Time listening during reception (should be equal or bigger than talking time)
	int   	_restTime;			// Rest time after a message has been sent before sending new messages
	float 	_talkingPeriod;		// Talking period (between messages)
	boolean _talking;			// Talking flag
	boolean _receiving;			// Receiving flag
	boolean _armed;				// Prepared to send a message (waiting for medium to be free)
	int     _messageTypeToSend;			// Counter to select the message type to send (color, period, sync) 
	int     _delay;				// Random delay to start talking when armed (to avoid collisions with other pins)

	// Application layer
	String 	_toSendMsg;			// Message to send (when possible)
	String 	_receivedMsg;		// Message received (will be sent when possible)
	String 	_lastReceivedMsg;	// Last received message (debugging purposes) 

	// Behavior variables
	float 	_color;				// Color (current) normalized 0-1 
	float 	_colorPeriod;		// Color period (current) in ms
	float   _targetColor;		// Color to go to
	float   _targetColorPeriod;	// Period to go to	boolean _armed;				// Prepared to talk flag
	float   _acummulator;		// Internal accumulator
	float   _targetAccumulator;	// For syncing
	int     _receivedFlashCnt;	// Counter used to flash light when message received
	
	long  _pinTime;				// Internal pin time 
	long  _nextMessageTime;		// Time for next message to send

	// Simulation variables
	Pin		_receivingPin;     	// in that is talking to me (will be used to check collisions) 

	// Initializing values
	int 	colorPeriodRangeGlb    =    50;
	int 	colorPeriodMinGlb      =    10;
	int 	talkingPeriodRangeGlb  =    10;
	int 	talkingPeriodMinGlb    =    50;
	int 	talkTimeGlb            =     6;
	int 	releaseTimeGlb         =   100;
	int		waitingRandomTimeGlb   =    11;
	int		receivedFlashLengthGlb =     2;
	float   colorChangeSpeedGlb    = 0.01f;	// Speed to approximate to new color
	float   periodChangeSpeedGlb   = 0.01f;	// Speed to approximate to new period 

	Pin(PApplet p) {
		_parent           	= p;
		_colorPeriod      	= _parent.random(0.1f, 1f);
		_targetColorPeriod	= _parent.random(0.1f, 1f);
		_talkingPeriod    	= _parent.random(0.1f, 1f);
		_color            	= _parent.random(1f);
		_targetColor      	= _parent.random(1f);
		_r                	= 0.01f;
		_beta             	= _parent.random(-0.5f, 0.5f);
		_alfa    		  	= _parent.random( 0.3f, 0.4f);
		_power   		  	= _parent.random( 0.3f, 0.4f);
		_armed   		  	= false;
		_talking 		  	= false;
		_pinTime 		  	= 0;
		_restTime 			= 0;
		_messageTypeToSend			= 0;
		_receivedTime		= 0;
		_nextMessageTime 	= (long)(_talkingPeriod * talkingPeriodRangeGlb + talkingPeriodMinGlb);

	}

	Pin(PApplet p, float x, float y) {
		this(p);
		setPos(x, y);
	}

	void arm(String msg) {
		// if ready to arm (not inhibited during rest time)
		if (_restTime == 0) {
			_armed       = true;
			_toSendMsg   = msg;
			_delay 		 = (int)_parent.random(waitingRandomTimeGlb); // Wait a random time to access the medium
		}
	}

	void talk() {
		// if armed and not receiving (and random access time finished)
		if (_armed  && _receivedTime == 0 && _delay == 0) {
			_armed    = false;
			_talking  = true;						// Ok, I am talking
			_talkTime = talkTimeGlb;				// I will be talking for a while
			((Simulation) _parent).msgSentGlb++;	// Global counter of messages sent
		} else {
			// if finish talking
			if (_talkTime <= 0 && _talking) {
				_talking   = false;
				_toSendMsg = null;
				_restTime = releaseTimeGlb;			// I will not talk again for a while
			}
		}
	}

	void listen(Pin talkingPin) {
		// If someone is talking with enough power so I can listen it
		if (talkingPin._talking && PApplet.dist(talkingPin._x, talkingPin._y, _x, _y) < _power) {
			float b = PApplet.atan2(_y - talkingPin._y, _x - talkingPin._x);
			// And I am listening in the correct angle (I see it)
			if (b >= talkingPin._beta - PApplet.PI / 2 * talkingPin._alfa && 
				b <= talkingPin._beta + PApplet.PI / 2 * talkingPin._alfa) {
				_receiving = true;
				// If I am not already listening to other pin or have had any interference 
				if (_receivingPin == null) {
					_receivingPin = talkingPin;		// Ok, this pin is talking me
					_receivedTime++; 				// Count time, to see if I can get the whole message (simulation)
				} else {		
					// PApplet.println(_receivingPin + " " + talkingPin);
					// If I am listening to the same pin
					if (_receivingPin == talkingPin) {
						// Increase time I've been listening to message
						_receivedTime++; 
						// if the time I've been listening is enough to receive the whole message
						if (_receivedTime >= talkTimeGlb) {
							// If I have not received any other message during this message period 
							if (talkingPin._toSendMsg != null) {
								_receivedMsg = talkingPin._toSendMsg;
								((Simulation) _parent).lastReceivedMsgGlb = _receivedMsg;
								execMsg(_receivedMsg);
								_receivedTime = 0;
							}
							_lastReceivedMsg = _receivedMsg;
						}
					} else {
						_receivedMsg = "ERR_COLLISION";
						((Simulation) _parent).msgCollisionsGlb++;
					}
				}
			}
		}
	}

	void execMsg(String msg) {
		// PApplet.println("Received " + msg);
		((Simulation) _parent).msgReceivedGlb++;	
		String command = msg.substring(0, 3);
		if (command.equals("COL")) {
			// Received color, so apply (target color), and resend message
			_targetColor = (PApplet.parseFloat(msg.substring(4, 9))) / 10000;
			arm(msg);
			((Simulation) _parent).colMsgNumberGlb++;
		}
		if (command.equals("PER")) {
			// Received period, so apply (target period), and resend message
			_targetColorPeriod = PApplet.parseFloat(msg.substring(4, 9)) / 10000f;
			arm(msg);
			((Simulation) _parent).perMsgNumberGlb++;
		}
		if (command.equals("SYN")) {
			// Received sync, so apply (synchronize), and resend message
			// after increasing sequence number (number of hops)
			int hops = PApplet.parseInt(msg.substring(4, 9));
			_targetAccumulator = PApplet.constrain(1.0f - hops / _parent.frameRate, 0f, 1f);
			hops++;
			((Simulation) _parent).maxSeqNumberGlb = 
				(long) PApplet.max(((Simulation) _parent).maxSeqNumberGlb, hops);
			arm("SYN_" + PApplet.nf(hops, 5, 0));
			((Simulation) _parent).synMsgNumberGlb++;
		}
		if (command.equals("LON")) {
		}
		if (command.equals("LOF")) {
		}
		if (command.equals("ERR")) {
		}
		
		_receivedFlashCnt = receivedFlashLengthGlb;
		_receivedMsg = null;
	}

	void update() {
		// Color and period follower
		_color = _color + (_targetColor - _color) * colorChangeSpeedGlb;
		_colorPeriod = _colorPeriod + (_targetColorPeriod - _colorPeriod) * periodChangeSpeedGlb;
		
		// 
		if (_targetAccumulator > 0) {
			if (_acummulator > 0.5) {
				_acummulator = (1 - _acummulator) * _targetAccumulator;
			} else {
				_acummulator *= _targetAccumulator;
			}
			_targetAccumulator -= 1.0 / _parent.frameRate;
		} else {
			_acummulator += _colorPeriod / _parent.frameRate;
		}
		if (_acummulator > 1) {
			_acummulator = 0;
		}
		
		// Own initiative communication (when not repeating received message)
		// perhaps this type of message should be more important than repeated messages
		if (_pinTime > _nextMessageTime) {
			if (_messageTypeToSend == 0) {
				arm("COL_" + PApplet.nf((int) (_color * 10000), 5, 0));
				((Simulation) _parent).colMsgNumberGlb++;
			}
			if (_messageTypeToSend == 1) {
				arm("PER_"
						+ PApplet.nf((int) (_targetColorPeriod * 10000), 5, 0));
				((Simulation) _parent).perMsgNumberGlb++;
			}
			if (_messageTypeToSend == 2) {
				arm("SYN_" + PApplet.nf((int) 0, 5, 0));
				((Simulation) _parent).synMsgNumberGlb++;
			}
			
			// Change type for the next time
			_messageTypeToSend = (_messageTypeToSend + 1) % 3;
			
			// Program next talking time
			float t = (_talkingPeriod * talkingPeriodRangeGlb + talkingPeriodMinGlb);
			_nextMessageTime = _pinTime + (long)t;
			
		}

		talk();

		_pinTime++;
		if (_talkTime != 0) _talkTime--;
		if (_restTime != 0) _restTime--;
		if (_delay != 0) 	_delay--;

	}

	void setPos(float x, float y) {
		_x = x;
		_y = y;
	}

	void setBeta(float b) {
		_beta = b;
	}

	// void draw(float _x, float _y) {
	void draw() {
		_parent.pushStyle();

		PVector c = new PVector(0, 0, 0);
		float   alfaChannel = 0;
		
		if (((Simulation) _parent).bDrawDebug || false) {
			
			if (_armed) {
				c.set(1, 1, 0);
			}
	
			if (_talking) {
				c.set(1, 0, 0);
			}
	
			if (_restTime > 0) {
				c.set(0, 1, 0);
			}
			
			if (_receivedFlashCnt-- > 0) {
				c.set(1, 1, 1);
			}
	
			_parent.fill(c.x * 255, c.y * 255, c.z * 255, 150);
			_parent.ellipse(_parent.width * _x, _parent.height * _y, 
					_parent.width * _r * 2, _parent.width * _r * 2);

			_parent.fill(255);
			_parent.text(_receivedTime, _parent.width * _x, _parent.height * _y - 20);
			_parent.text(_restTime, _parent.width * _x, _parent.height * _y + 20);

		}
		
		alfaChannel = 255 * PApplet.sq(PApplet.sq((1 - 2 * PApplet.abs(_acummulator - 0.5f))));
		c = ((Simulation) _parent).mapColor(PApplet.constrain(_color, 0, 1));

		if (_talking) {
			c.set(1, 1, 1);
			alfaChannel = 200;
		}
		
		// Contour
		_parent.stroke(200, 10);
		_parent.ellipseMode(PApplet.CENTER);

		// Light
		_parent.fill(c.x * 255, c.y * 255, c.z * 255, alfaChannel);
		_parent.ellipse(_parent.width * _x, _parent.height * _y, 
						_parent.width * _r, _parent.width * _r);

		// IR active
		if (_talking && ((Simulation) _parent).bDrawIrGlb) {
			_parent.noFill();
			_parent.stroke(200, 100);
			_parent.line(_parent.width * _x, _parent.height * _y, 
					_parent.width * _x + _parent.width * _power 
					* PApplet.cos(_beta * PApplet.TWO_PI - PApplet.PI / 2 * _alfa), 
					_parent.height * _y	+ _parent.width	* _power
					* PApplet.sin(_beta * PApplet.TWO_PI - PApplet.PI / 2 * _alfa));
			_parent.line(_parent.width * _x,	_parent.height * _y,
					_parent.width * _x + _parent.width * _power
					* PApplet.cos(_beta * PApplet.TWO_PI + PApplet.PI / 2 * _alfa),
					_parent.height * _y	+ _parent.width	* _power
					* PApplet.sin(_beta * PApplet.TWO_PI + PApplet.PI / 2 * _alfa));
			_parent.arc(_parent.width * _x, _parent.height * _y, 
					_parent.width * _power * 2, _parent.width * _power * 2, 
					_beta * PApplet.TWO_PI - PApplet.PI / 2 * _alfa, 
					_beta * PApplet.TWO_PI + PApplet.PI / 2 * _alfa);
		}

		_parent.popStyle();
	}
	
	String stats() {
		String txt = 
				"\nColor   : " + _color +
				"\nPower   : " + _power +
				"\nCPeriod : " + _colorPeriod +
				"\nTPeriod : " + _talkingPeriod +
				"\nArmed   : " + _armed +
				"\nTalking : " + _talking +
				"\nSMsg    : " + _toSendMsg +
				"\nRMsg    : " + _receivedMsg +
				"\nLMsg    : " + _lastReceivedMsg +
				"\nTTime   : " + _talkTime +
				"\nRTime   : " + _restTime +
				"\nRecTime : " + _receivedTime +
				"\nCap     : " + _acummulator;
		return txt;
	}

}