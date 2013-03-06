package art.petaka.pincells;

import processing.core.*;
import controlP5.*;

public class Simulation extends PApplet {

	private static final long serialVersionUID = 1L;

	// Frame Rate
	final int FRAME_RATE  = 60;
	// NUmber of persons/pins
	final int NUM_PERSONS = 800;

	// Names of gens
	final String[] GEN_NAMES = { 
		"Intensity", 
		"Inertia", 
		"Personality",
		"Attention", 
		"Range", 
		"Color", 
		"Period" 
	};

	// Gan values (min, max)
	final float GEN_VALUES[][] = { { 0.01f, 1.0f }, // Intensity
			{ 0.01f, 1.0f }, // Inertia
			{ 0.01f, 1.0f }, // Personality
			{ 0.01f, 1.0f }, // Attention
			{ 0.01f, 1.0f }, // Range
			{ 0.01f, 1.0f }, // Color
			{ 0.01f, 1.0f }, // Period
	};

	// Global drawing flags
	boolean bPauseGlb 		= false;
	boolean bDrawIrGlb 		= false;
	boolean bDrawBackground = true;
	boolean bDrawDebug 		= false;


	// Global messages counters
	long colMsgNumberGlb;
	long perMsgNumberGlb;
	long synMsgNumberGlb;
	long maxSeqNumberGlb;
	long msgSentGlb;
	long msgReceivedGlb;
	long msgCollisionsGlb;
	
	
	String lastReceivedMsgGlb;

	// Array of persons/pins
	Person personsArrayGlb[];

	// Graphic mean gen object
	BarGraph selectedGen;

	// Fonts
	PFont fontA;

	// Control objects
	ControlP5 controlP5;

	public void setup() {
		// size(screenWidth, screenHeight);
		size(1024, 768);
		// size(640, 480, GLConstants.GLGRAPHICS );
		// size(640, 480, OPENGL);
		smooth();
		frameRate(FRAME_RATE);
		strokeWeight(1);

		fontA = createFont("FFScala", 32, true);
		textFont(fontA, 12);

		controlP5 = new ControlP5(this);
		setupControls();
		createPersons();
		updateControlValues();

		background(0);

	}
	
	void createPersons() {
		personsArrayGlb = new Person[NUM_PERSONS];
		for (int i = 0; i < NUM_PERSONS; i++) {
			// personsArrayGlb[i] = new Person(this, 0.02f + 1.0f / NUM_PERSONS * i, 0.75f);
			personsArrayGlb[i] = new Person(this);
		}	
	}

	// Person behavior parameters
	Range intensityGlb;
	Range inertiaGlb;
	Range personalityGlb;
	Range attentionGlb;
	Range rangeGlb;
	Range colorGlb;
	Range PeriodGlb;

	// Pin behavior parameters
	Range  colorPeriodGlbRng;
	Range  talkingPeriodGlbRng;
	Slider colorChangeSpeedGlbSld;
	Slider periodChangeSpeedGlbSld;

	// Pin communication parameters
	Slider irEmmitingAngleGlbSld;
	Slider irEmmitingPowerGlbSld;
	Slider talkingTimeGlbSld;
	Slider releaseTimeGlbSld;

	void setupControls() {

		int row = 300;
		int col = 20;
		int inc = 20;
		int l = 200;
		int h = 12;


		controlP5.addTextlabel("IR Params", "IR Parameters:", col, row += inc);
		irEmmitingAngleGlbSld 	= controlP5.addSlider("Emmiting Angle", 0,  360,  120, col, row += inc, l, h);
		irEmmitingPowerGlbSld 	= controlP5.addSlider("Emmiting Power", 0,   50,   10, col, row += inc, l, h);
		talkingTimeGlbSld 		= controlP5.addSlider("Talking Time",   5,  750,   25, col, row += inc, l, h);
		releaseTimeGlbSld 		= controlP5.addSlider("Release Time", 100, 5000, 1500, col, row += inc, l, h);
		talkingPeriodGlbRng 	= controlP5.addRange("Talking Period Range", 1, 30, 15, 25, col, row += inc, l, h);
		
		controlP5.addTextlabel("PIN Params", "PIN Parameters:", col, row += 2 * inc);
		colorChangeSpeedGlbSld 	= controlP5.addSlider("Color Change Speed", 0.1f, 10, 1, col, row += inc, l, h);
		periodChangeSpeedGlbSld = controlP5.addSlider("Period Change Speed", 0.1f, 10, 1, col, row += inc, l, h);
		colorPeriodGlbRng 		= controlP5.addRange("Color Period Range", 1, 15, 3, 6, col, row += inc, l, h);
		
		// controlP5.load("defaults.txt");

	}
	
	void updateControlValues() {
		for (int i = 0; i < personsArrayGlb.length; i++) {
			// IR parameters
			personsArrayGlb[i]._pin._alfa = irEmmitingAngleGlbSld.value() / 180f;
			personsArrayGlb[i]._pin._power = irEmmitingPowerGlbSld.value() / 100f;
			personsArrayGlb[i]._pin.talkTimeGlb = (int) (talkingTimeGlbSld.value() / 1000 * FRAME_RATE) + 1;
			personsArrayGlb[i]._pin.releaseTimeGlb = (int) (releaseTimeGlbSld.value() / 1000 * FRAME_RATE) + 1;
			personsArrayGlb[i]._pin.talkingPeriodMinGlb = (int)(talkingPeriodGlbRng.lowValue() * FRAME_RATE) + 1;
			personsArrayGlb[i]._pin.talkingPeriodRangeGlb = 
					(int)((talkingPeriodGlbRng.highValue() - talkingPeriodGlbRng.lowValue()) * FRAME_RATE) + 1;

			// PIN Parameters
			personsArrayGlb[i]._pin.colorChangeSpeedGlb = colorChangeSpeedGlbSld.value() / 100f;
			personsArrayGlb[i]._pin.periodChangeSpeedGlb = periodChangeSpeedGlbSld.value() / 100f;
			personsArrayGlb[i]._pin._colorPeriod = random(colorPeriodGlbRng.lowValue(),	colorPeriodGlbRng.highValue());
		}
	}

	public void controlEvent(ControlEvent theControlEvent) {

		// IR parameters
		if (theControlEvent.label() == "Emmiting Angle") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin._alfa = irEmmitingAngleGlbSld.value() / 180f;
			}
		}
		if (theControlEvent.label() == "Emmiting Power") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin._power = irEmmitingPowerGlbSld.value() / 100f;
			}
		}
		if (theControlEvent.label() == "Talking Time") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin.talkTimeGlb = (int) (talkingTimeGlbSld.value() / 1000 * FRAME_RATE) + 1;
			}
		}
		if (theControlEvent.label() == "Release Time") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin.releaseTimeGlb = (int) (releaseTimeGlbSld.value() / 1000 * FRAME_RATE) + 1;
			}
		}
		if (theControlEvent.label() == "Talking Period Range") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin.talkingPeriodMinGlb = 
						(int)(talkingPeriodGlbRng.lowValue() * FRAME_RATE) + 1;
				personsArrayGlb[i]._pin.talkingPeriodRangeGlb = 
						(int)((talkingPeriodGlbRng.highValue() - talkingPeriodGlbRng.lowValue()) * FRAME_RATE) + 1;
			}
		}

		// PIN Parameters
		if (theControlEvent.label() == "Color Change Speed") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin.colorChangeSpeedGlb = colorChangeSpeedGlbSld.value() / 100f;
			}
		}
		if (theControlEvent.label() == "Period Change Speed") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin.periodChangeSpeedGlb = periodChangeSpeedGlbSld.value() / 100f;
			}
		}
		if (theControlEvent.label() == "Color Period Range") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin._colorPeriod = random(
						colorPeriodGlbRng.lowValue(),
						colorPeriodGlbRng.highValue());
			}
		}
	}

	String stats() {
		float meanPeriod = 0;
		float meanColor = 0;
		float sdevPeriod = 0;
		float sdevColor = 0;
		for (int i = 0; i < NUM_PERSONS; i++) {
			meanColor  += personsArrayGlb[i]._pin._targetColor;
			meanPeriod += personsArrayGlb[i]._pin._targetColorPeriod;
		}
		meanColor  /= NUM_PERSONS;
		meanPeriod /= NUM_PERSONS;

		for (int i = 0; i < NUM_PERSONS; i++) {
			sdevColor  += sq(personsArrayGlb[i]._pin._targetColor - meanColor);
			sdevPeriod += sq(personsArrayGlb[i]._pin._targetColorPeriod	- meanPeriod);
		}
		sdevColor  /= NUM_PERSONS;
		sdevPeriod /= NUM_PERSONS;
		sdevColor   = sqrt(sdevColor);
		sdevPeriod  = sqrt(sdevPeriod);

		String res = "Mean Per : " + nf((int) (meanPeriod * 10000), 4)
				+ "\nSdev Per : " + nf((int) (sdevPeriod * 10000), 4)
				+ "\nMean Col : " + nf((int) (meanColor * 10000), 4)
				+ "\nSdev Col : " + nf((int) (sdevColor * 10000), 4)
				+ "\n\nSent   : " + msgSentGlb
				+ "\nColls    : " + msgCollisionsGlb
				+ "\nReceived : " + msgReceivedGlb
				+ "\n  Col    : " + colMsgNumberGlb 
				+ "\n  Per    : " + perMsgNumberGlb 
				+ "\n  Syn    : " + synMsgNumberGlb
				+ "\nMax Seq  : " + maxSeqNumberGlb 
				+ "\nLast Msg : " + lastReceivedMsgGlb;

		return res;
	}

	int lastI = 0;
	String stTxt = "";

	public void draw() {
		if (bPauseGlb)
			return;

		if (bDrawBackground)
			background(0);
		// fill(0, 0);
		// rect(0, 0, width, height);

		for (int i = 0; i < NUM_PERSONS; i++) {
			personsArrayGlb[i].update();
			// personsArrayGlb[i].draw();
			personsArrayGlb[i]._pin.update();
			personsArrayGlb[i]._pin.draw();
			if (mousePressed) {
				if (dist((float)mouseX / width, (float)mouseY / height,
						personsArrayGlb[i]._pin._x, personsArrayGlb[i]._pin._y) < personsArrayGlb[i]._pin._r) {
					personsArrayGlb[i]._pin.arm("SYN_" + PApplet.nf((int) 0, 5, 0));;
					if (keyPressed) lastI = i;
					break;
				}
			} else {

			}
		}

		if (true) {
			fill(255);
			String txt = personsArrayGlb[lastI]._pin.stats();
			text(txt, width - 200,  50);
			text(lastI,   width / 2 , 50);
			personsArrayGlb[lastI]._expression.draw(width / 2, 70);
		}
		
		if (frameCount % (int) frameRate == 0) {
			stTxt = stats();
		}

		fill(255);
		text(stTxt, 20, 50);

	}

	PVector mapColor(float c) {
		float s = 1.0f / 6;
		float R, G, B;
		if (c < s) {
			R = c / s;
			G = 0;
			B = 1;
			return new PVector(R, G, B);
		} else if (c < 2 * s) {
			c = c - 1 * s;
			R = 1;
			G = 0;
			B = 1 - c / s;
			return new PVector(R, G, B);
		} else if (c < 3 * s) {
			c = c - 2 * s;
			R = 1;
			G = c / s;
			B = 0;
			return new PVector(R, G, B);
		} else if (c < 4 * s) {
			c = c - 3 * s;
			R = 1 - c / s;
			G = 1;
			B = 0;
			return new PVector(R, G, B);
		} else if (c < 5 * s) {
			c = c - 4 * s;
			R = 0;
			G = 1;
			B = c / s;
			return new PVector(R, G, B);
		} else {
			c = c - 5 * s;
			R = 0;
			G = 1 - c / s;
			B = 1;
			return new PVector(R, G, B);
		}
	}

	public void keyPressed() {
		switch (key) {
		case ' ':
			bPauseGlb = !bPauseGlb;
			break;
		case 'i':
			bDrawIrGlb = !bDrawIrGlb;
			break;
		case 'b':
			bDrawBackground = !bDrawBackground;
			break;
		case 'r':
			createPersons();
			break;
		case 's':
			controlP5.save("defaults.txt");
			break;
		}
	
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "--bgcolor=#F0F0F0",
				"art.petaka.pincells.Simulation" });
	}

}
