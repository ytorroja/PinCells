package art.petaka.pincells;

import processing.core.*;

public class Person {
	Simulation _parent;

	Chromosome _expression;
	Chromosome _desire;
	
	Tail _tail;
	
	float _x, _y, _dd, _td;
	
	float _inertia; 
	float _intensity;
	float _range;
	float _attention;
	float _personality;
	float _period;
	float _color;
	
	PVector _towards;
	float   _offset;
	
	Pin     _pin;

	Person _personArray[];
	
	Person(Simulation p) {
		_parent = p;
		_personArray = _parent.personsArrayGlb;
		_expression  = new Chromosome(_parent, _parent.GEN_NAMES, _parent.GEN_VALUES);
		_desire      = new Chromosome(_parent, _parent.GEN_NAMES, _parent.GEN_VALUES);
		_x = _parent.random(0, 1);
		_y = _parent.random(0, 1);
		_towards = new PVector(0, 0);
		
		_intensity   = _expression.val("Intensity");
		_inertia     = _expression.val("Inertia");
		_range       = PApplet.constrain(_expression.val("Range"), 0.05f, 1f);
		_attention   = _expression.val("Attention");
		_personality = PApplet.constrain(_expression.val("Personality") * 1.1f, 0f, 0.99f);
		_color       = _expression.val("Color");
		_period      = _expression.val("Period");		
		
		// _tail = new Tail(new PVector(width * _x, height * _y));
		_tail        = new Tail(_parent, new PVector(_x, _y));
		_offset      = (int) _parent.random(0, 60);
		_pin         = new Pin(_parent, _x, _y);
	}

	float affinity(Person otherCell) {
		return _desire.distance(otherCell._expression);
	}

	void update() {
		PVector result = new PVector(0, 0);
		PVector inc = new PVector(0, 0);
		
		for (int i = 0; i < _parent.NUM_PERSONS; i++) {
			float distance = PApplet.dist(_x, _y, _personArray[i]._x, _personArray[i]._y);
			if (distance < _range) {
				float aff = this.affinity(_personArray[i]);
				float desiredDistance = PApplet.map(aff, 0f, (float)_expression.size(), 0.1f, 1f) / 10f; 
				_dd = desiredDistance;
				
				float difference = PApplet.dist(_x, _y, _personArray[i]._x, _personArray[i]._y) - desiredDistance;
				_td = PApplet.constrain(difference - _td, -1f, 2f);
				
				inc.x = (_personArray[i]._x - _x);
				inc.y = (_personArray[i]._y - _y);
				inc.normalize();
				inc.mult(_td);
				result.add(inc);
				/*
				_parent.stroke(255);
				_parent.fill(255);
				_parent.line(_x * _parent.width,  _y * _parent.height, (_x + inc.x) * _parent.width,  (_y + inc.y) * _parent.height);
				*/
			}

			// if (this._pin._talking) println("This = " +
			// this._pin._sendingMsg);
			if (!_personArray[i].equals(this))
				_personArray[i]._pin.listen(this._pin);

			/*
			 * if (_pin._receivedMsg != "") {
			 * CellArrayGlb[i]._pin.arm(_pin._receivedMsg); }
			 */

		}

		/*

		*/
		result.mult(_intensity / 100);
		result.sub(_towards);
		result.mult(_inertia / 90);
		result.add(_towards);
		if (_parent.random(0, 1) < _attention) {
			result.mult(0.2f);
		}
		result.mult(0.9f);
		_towards = result;

		// Update position
		_x = _x + _towards.x;
		_y = _y + _towards.y;
		_pin.setPos(_x, _y);
		_pin.setBeta(PApplet.atan2(_towards.y, _towards.x) / PApplet.TWO_PI);

		// Clipped or toroidal space
		boolean toroidal = true;
		if (toroidal) {
			// Toroidal space
			if (_x < 0)
				_x = 1 + _x;
			if (_y < 0)
				_y = 1 + _y;
			if (_x > 1)
				_x = _x - 1;
			if (_y > 1)
				_y = _y - 1;
		} else {
			// Clipped space
			_x = PApplet.constrain(_x, 0.05f, 0.95f);
			_y = PApplet.constrain(_y, 0.05f, 0.95f);
		}

		// Tail Update
		// _tail.update(new PVector(width * _x, height * _y));
		_tail.update(new PVector(_x, _y));
	}

	void draw() {
		_parent.pushMatrix();
		_parent.pushStyle();
		// Color computation
		float p = _period * 100 + 20;
		float alfa = 255 * PApplet.sq(PApplet.sq(2 * (PApplet.abs(((_parent.frameCount) % p) - p / 2) / p)));
		// float alfa = 50;
		// fill(255 * (1 - _r), 255 * _r , _color * 255, alfa);
		// fill(_color.x * 255, _color.y * 255, _color.z * 255, alfa);
		PVector c = _parent.mapColor(PApplet.constrain(_color, 0, 1));

		_parent.fill(c.x * 255, c.y * 255, c.z * 255, alfa);

		// Body
		// if (_resting < 10) stroke(200, 250); else stroke(200, 50);
		_parent.noFill();
		_parent.stroke(200, 80);
		_parent.ellipseMode(PApplet.CENTER);
		// ellipse(width * _x, height * _y,
		// 10 + width * _r / 20 , 10 + width * _r / 20 );
		_parent.ellipse(_parent.width * _x, _parent.height * _y, _parent.width * 0.02f, _parent.width * 0.02f);

		// Tail
		_tail.draw();
		_parent.popStyle();
		_parent.popMatrix();

	}

	void muteDesire(float depth) {
		_desire.mute(depth);
	}

	void muteExpression(float depth) {
		_expression.mute(depth);
		
		_intensity   = _expression.val("Intensity");
		_inertia     = _expression.val("Inertia");
		_range       = PApplet.constrain(_expression.val("Range"), 0.05f, 1f);
		_attention   = _expression.val("Attention");
		_personality = PApplet.constrain(_expression.val("Personality") * 1.1f, 0f, 0.99f);
		_color       = _expression.val("Color");
		_period      = _expression.val("Period");		
	}

}
