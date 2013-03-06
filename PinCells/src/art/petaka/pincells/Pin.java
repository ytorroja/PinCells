package art.petaka.pincells;

import processing.core.*;

public class Pin {
  PApplet _parent;
  
  // Core Variables
  float _x, _y;
  float _size;
  float _beta;
  float _color;
  float _range;
  float _alfa;
  float _colorPeriod;
  float _talkingPeriod;
  boolean _armed;
  boolean _talking;
  
  String _sendingMsg;
  String _receivedMsg;
  String _lastReceivedMsg;

  int colorPeriodRangeGlb    = 50;
  int colorPeriodMinGlb      = 10;
  int talkingPeriodRangeGlb  = 10;
  int talkingPeriodMinGlb    =  5;
  int talkTimeGlb            =  6;
  int releaseTimeGlb         = 100;

  float colorChangeSpeedGlb  = 0.01f;
  float periodChangeSpeedGlb = 0.01f;  
  
  // Temporal Auxiliar Variables  
  float _r;
  int   _talkTime;
  int   _releaseTime;
  float _targetColor;
  float _targetColorPeriod;
  float _capacitor;
  float _targetCapacitor;
  
  Pin(PApplet p) {
	_parent  			= p;
    _colorPeriod   		= _parent.random(0.1f, 1f);
    _targetColorPeriod 	= _parent.random(0.1f, 1f);
    _talkingPeriod 		= _parent.random(0.1f, 1f);
    _color   			= _parent.random(1f);
    _targetColor 		= _parent.random(1f);
    _size   			= _parent.random(1f);
    _r					= 0.01f;
    _beta    			= _parent.random(-0.5f, 0.5f);
    _alfa    			= _parent.random(0.3f, 0.4f); 
    _range   			= _parent.random(0.3f, 0.4f) / 3;
    _armed   			= false;
    _talking 			= false;
  }

  Pin(PApplet p, float x, float y) {
    this(p);
    setPos(x, y);
  }
  
  void arm(String msg) {
    if (_releaseTime <= 0) {
    // if (!_armed) {
      _armed     = true;
      _releaseTime = releaseTimeGlb;
      _sendingMsg = msg;
    }
  }
  
  void talk() {
    if (_armed) {
    // if (_armed && _capacitor == 0) {
      _armed = false;
      _talking = true;
      _talkTime = talkTimeGlb;
    } else {
      if (_talkTime <= 0) {
      // if (_capacitor == 0) {
        _talking = false;
        _sendingMsg = null;
      }
    }
  }
  
  void listen(Pin _p) {
    if (_p._talking && PApplet.dist(_p._x, _p._y, _x, _y) < _range) {
      float b = PApplet.atan2(_y - _p._y, _x - _p._x);
      if (b >= _p._beta - PApplet.PI/2 * _p._alfa && b <= _p._beta + PApplet.PI/2 * _p._alfa) {
       if (_receivedMsg == null && _p._sendingMsg != null) {
       // if (_receivedMsg == null) {
           _receivedMsg = _p._sendingMsg;    
          execMsg(_receivedMsg);
        } else {
          _receivedMsg = "ERR_RECEIVER";
        }
        _lastReceivedMsg = _receivedMsg;
      }
    }
  }

  void execMsg(String msg) {
    // println("-"+msg+"-");
    String command = msg.substring(0, 3);
    if (command.equals("COL")) {
      _targetColor += (PApplet.parseFloat(msg.substring(4, 9)))/10000;
      _targetColor /= 2;
      arm(msg);
      ((Simulation)_parent).colMsgNumberGlb++;
    }
    if (command.equals("PER")) {
      // _targetColorPeriod += float(msg.substring(4, 9))/10000;
      // _targetColorPeriod /= 2;
      _targetColorPeriod = PApplet.parseFloat(msg.substring(4, 9))/10000f;
      arm(msg);
      ((Simulation)_parent).perMsgNumberGlb++;
    }
    if (command.equals("SYN")) {
      int seq = PApplet.parseInt(msg.substring(4, 9));
      _targetCapacitor = PApplet.constrain(1.0f - seq / _parent.frameRate, 0f, 1f);
      seq++;
      ((Simulation)_parent).maxSeqNumberGlb = (long)PApplet.max(((Simulation)_parent).maxSeqNumberGlb, seq);
      arm("SYN_"+PApplet.nf(seq, 5, 0));
      ((Simulation)_parent).synMsgNumberGlb++;
    }    
    if (command.equals("LON")) {
    }    
    if (command.equals("LOF")) {
    }    
    if (command.equals("ERR")) {
    }    
    _receivedMsg = null;
  }

  void update() {
    // Variables updates
    // _beta += (noise(_r) - 0.5)/100;
    // _r += _parent.random(0.1f);
    _color = _color + (_targetColor - _color) * colorChangeSpeedGlb;
    _colorPeriod = _colorPeriod + (_targetColorPeriod - _colorPeriod) * periodChangeSpeedGlb;
    
    if (_targetCapacitor > 0) {
      if (_capacitor > 0.5) {
        _capacitor = (1 - _capacitor) * _targetCapacitor;
      } else {
        _capacitor *= _targetCapacitor;
      }
      _targetCapacitor -= 1.0 / _parent.frameRate;
    } else {
      _capacitor += _colorPeriod / _parent.frameRate;
    }
    if (_capacitor > 1) {
      _capacitor = 0;
    }
    
    // IR action range
    // float t = (_talkingPeriod * talkingPeriodRangeGlb + talkingPeriodMinGlb);
    
    /*
    if (frameCount % t < 0.01) {
      arm("COL_" + nf((int)(_color*10000),5,0));
    } else if (frameCount % t < 0.05) {
      // arm("PER_" + nf((int)(_targetColorPeriod*10000),5,0));
      arm("SYN_"); // + nf(int(_capacitor*10000),5,0));
    } else if (frameCount % t < 0.1) {
      arm("PER_" + nf((int)(_targetColorPeriod*10000),5,0));
      // arm("PER_" + nf(int(_capacitor*10000),5,0));
    }
    */
    if (_capacitor == 0) {
      if (_parent.random(1) < 0.01) {
        arm("COL_" + PApplet.nf((int)(_color*10000), 5, 0));
        ((Simulation)_parent).colMsgNumberGlb++;
      }
      if (_parent.random(1) < 0.01) {
        arm("PER_" + PApplet.nf((int)(_targetColorPeriod*10000), 5, 0));
        ((Simulation)_parent).perMsgNumberGlb++;
      } 
      if (_parent.random(1) < 0.01) {
        arm("SYN_" + PApplet.nf((int)0 , 5, 0)); 
        ((Simulation)_parent).synMsgNumberGlb++;
      }
    }

    
    talk();
    
    _talkTime--;
    _releaseTime--;
 }
  
  
  void setPos(float x, float y) {
    _x = x;
    _y = y; 
  }
  
  void setBeta(float b) {
    _beta = b; 
  }
  
  //void draw(float _x, float _y) {
  void draw() {
    _parent.pushStyle();
    
    // Color computation
    // int p = (int)(_colorPeriod * colorPeriodRangeGlb + colorPeriodMinGlb);
    // float alfa = 255 * sq(sq((2 * (abs((frameCount % p) - (p / 2.)) / p))));
    float alfaChannel = 255 * PApplet.sq(PApplet.sq((1 - 2 * PApplet.abs(_capacitor - 0.5f))));
    PVector c = ((Simulation) _parent).mapColor(PApplet.constrain(_color, 0, 1));
    if (_talking && true) {
      c.set(1, 1, 1);
      alfaChannel = 200 * (1 - 2 * PApplet.abs(_talkTime - (float)talkTimeGlb / 2.0f) / talkTimeGlb);
    }
  
    // Contour
    _parent.stroke(200, 10);
    _parent.ellipseMode(PApplet.CENTER);

    // Light
    _parent.fill(c.x * 255, c.y * 255, c.z * 255, alfaChannel);
    _parent.ellipse(_parent.width * _x, _parent.height * _y, 
    		_parent.width * 0.01f + _size / 100 , _parent.width * 0.01f + _size / 100); 
             
    // IR active
    if (_talking && ((Simulation) _parent).bDrawIrGlb) {
    	_parent.noFill();
    	_parent.stroke(200, 100);
    	_parent.line(_parent.width * _x, _parent.height * _y, 
    			_parent.width * _x + _parent.width * (0.03f + _range) / 2 * PApplet.cos(_beta * PApplet.TWO_PI - PApplet.PI/2 * _alfa), 
    			_parent.height * _y + _parent.width * (0.03f + _range) / 2 * PApplet.sin(_beta * PApplet.TWO_PI - PApplet.PI/2 * _alfa));
    	_parent.line(_parent.width * _x, _parent.height * _y, 
    			_parent.width * _x + _parent.width * (0.03f + _range) / 2 * PApplet.cos(_beta * PApplet.TWO_PI + PApplet.PI/2 * _alfa), 
    			_parent.height * _y + _parent.width * (0.03f + _range) / 2 * PApplet.sin(_beta * PApplet.TWO_PI + PApplet.PI/2 * _alfa));
    	_parent.arc(_parent.width * _x, _parent.height * _y, 
    			_parent.width * (0.03f + _range), _parent.width * (0.03f + _range), 
          _beta * PApplet.TWO_PI - PApplet.PI/2 * _alfa, _beta * PApplet.TWO_PI + PApplet.PI/2 * _alfa);
    }
    
    _parent.popStyle();
  }    
      
}