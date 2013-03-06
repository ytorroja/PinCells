package art.petaka.pincells;

import processing.core.*;

public class BarGraph {
	PApplet _parent;
	
	final float cfBarAlphaGlb = 0.5f;
	
	float _x, _y;
	float _w, _h;
	float _val;
	PVector _col;
	String _txt;

	BarGraph(PApplet p, float x, float y, float w, float h, float val) {
		_parent = p;
		_x = x;
		_y = y;
		_w = w;
		_h = h;
		_col = new PVector(0.0f, 1.0f, 0.3f);
		_val = val;
		_txt = "";
	}

	BarGraph(PApplet p) {
		this(p, 0.5f, 0.5f, 0.1f, 0.02f, 0.5f);
	}

	BarGraph(PApplet p, String txt) {
		this(p);
		_txt = txt;
	}

	void setPos(float x, float y) {
		_x = x;
		_y = y;
	}

	void setSize(float w, float h) {
		_w = w;
		_h = h;
	}

	void setVal(float val) {
		_val = val;
	}

	void setCol(PVector col) {
		_col = col;
	}

	void setText(String txt) {
		_txt = txt;
	}

	void update() {
	}

	void draw() {
		_parent.pushMatrix();
		_parent.pushStyle();
		_parent.stroke(255, cfBarAlphaGlb * 255);
		_parent.strokeWeight(1);
		_parent.noFill();
		_parent.rect(_x * _parent.width, _y * _parent.height, _w * _parent.width, _h * _parent.height);
		_parent.stroke(255, cfBarAlphaGlb * 255);
		_parent.fill(_col.x * 255, _col.y * 255, _col.z * 255, cfBarAlphaGlb * 255);
		_parent.rect(_x * _parent.width, _y * _parent.height, _w * _parent.width * _val, _h * _parent.height);
		_parent.fill(255, cfBarAlphaGlb * 255);
		_parent.textFont(((Simulation)_parent).fontA, 11 * _h / 0.01f);
		_parent.text(_txt, (_x + _w) * _parent.width + _parent.textWidth("A"), (_y + _h) * _parent.height - 1);
		_parent.popStyle();
		_parent.popMatrix();
	}
}