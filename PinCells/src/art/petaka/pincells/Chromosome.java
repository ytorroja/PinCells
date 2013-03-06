package art.petaka.pincells;

import processing.core.*;

public class Chromosome {
	// Processing PApplet
	PApplet _parent;

	// Number of gens per chromosome
	int _numGens;

	// Gen object
	class Gen {
		float  value;
		String name;
		float  mute;
	}
	
	// Gen object array
	Gen _genArray[];

	// Graphic object array
	BarGraph  _barArray[];

	Chromosome(PApplet p, String names[], float values[][]) {
		_numGens = names.length;
		_parent = p;
		
		_genArray = new Gen[_numGens];

		_barArray = new BarGraph[_numGens];

		for (int i = 0; i < _numGens; i++) {
			_genArray[i] = new Gen();
			_genArray[i].value = _parent.random(values[i][0], values[i][1]);
			_genArray[i].name  = names[i];
			_genArray[i].mute  = _parent.random(0, 100f);
			
			_barArray[i] = new BarGraph(_parent);
			_barArray[i].setText(_genArray[i].name);
			_barArray[i].setSize(0.05f, 0.01f);
			_barArray[i].setPos(0f, i * (_barArray[i]._h * 1.4f));
		}
	}

	// Set by number function
	void set(int i, float val) {
		_genArray[i].value = val;
		
	}
	
	// Set by name function
	void set(String name, float val) {
		for(int i = 0; i < size(); i++) {
			if (name.toLowerCase() == _genArray[i].name.toLowerCase()) {
				_genArray[i].value = val;
				break;
			}
		}
	}
	
	// Compute distance between two chromosomes
	float distance(Chromosome otherCromosome) {
		float res = 0;
		for (int i = 0; i < _numGens; i++) {
			res += PApplet.sq(this._genArray[i].value - otherCromosome._genArray[i].value);
		}
		return res;
	}

	// Mute the gens (depth must be < 1.0)
	void mute(float depth) {
		for (int i = 0; i < _numGens; i++) {
			_genArray[i].mute = _genArray[i].mute + depth;
			_genArray[i].value += (_parent.noise(_genArray[i].mute) - 0.5) / 1000;
		}
	}

	// Number of gens in the chromosome
	int size() {
		return _numGens;
	}

	// Value of gen i
	float val(int i) {
		return _genArray[i].value;
	}

	// Value of gen whose name matches name
	float val(String name) {
		for(int i = 0; i < size(); i++) {
			if (name.toLowerCase() == _genArray[i].name.toLowerCase()) {
				return _genArray[i].value;
			}
		}
 		return -1;
	}

	// Name of gen i
	void setCromoName(int i, String str) {
		_genArray[i].name = str;
		_barArray[i].setText(str);
	}
	
	// Draw of gen values
	void draw(float x, float y) {
		_parent.pushMatrix();
		_parent.translate(x, y);
		for (int i = 0; i < size(); i++) {
			_barArray[i].setVal(_genArray[i].value);
			_barArray[i].draw();
		}
		_parent.popMatrix();
	}

}
