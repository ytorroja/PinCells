package art.petaka.pincells;

import processing.core.*;

class Tail {
	PApplet parent;

	int tlen = 10;

	PVector tail[];

	Tail(PApplet p) {
		parent = p;
		tail = new PVector[tlen];
		for (int i = 0; i < tlen; i++)
			tail[i] = new PVector(0, 0);
	}

	Tail(PApplet p, PVector npos) {
		parent = p;
		tail = new PVector[tlen];
		for (int i = 0; i < tlen; i++)
			tail[i] = new PVector(npos.x, npos.y);
	}

	void set(PVector npos) {
		tail = new PVector[tlen];
		for (int i = 0; i < tlen; i++)
			tail[i] = new PVector(npos.x, npos.y);
	}

	void update(PVector npos) {
		for (int i = tlen - 1; i > 0; i--) {
			tail[i] = tail[i - 1];
		}
		tail[0] = npos;
	}

	void draw() {
		parent.pushStyle();
		parent.stroke(100, 50);
		for (int i = 0; i < tlen - 1; i++) {
			parent.line(tail[i].x, tail[i].y, tail[i + 1].x, tail[i + 1].y);
		}
		parent.stroke(255);
		parent.popStyle();
	}

}
