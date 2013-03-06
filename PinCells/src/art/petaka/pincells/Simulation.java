package art.petaka.pincells;

import processing.core.*;
import controlP5.*;

public class Simulation extends PApplet {

	private static final long serialVersionUID = 1L;

	// NUmber of persons/pins
	final int NUM_PERSONS = 800;

	// Names of gens
	final String[] GEN_NAMES = { "Intensity", "Inertia", "Personality",
			"Attention", "Range", "Color", "Period" };

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
	boolean bPauseGlb = false;
	boolean bDrawIrGlb = false;

	// Global messages counters
	long colMsgNumberGlb;
	long perMsgNumberGlb;
	long synMsgNumberGlb;
	long maxSeqNumberGlb;

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
		frameRate(60);
		strokeWeight(1);

		fontA = createFont("FFScala", 32, true);
		textFont(fontA, 12);

		controlP5 = new ControlP5(this);
		setupControls();

		personsArrayGlb = new Person[NUM_PERSONS];
		for (int i = 0; i < NUM_PERSONS; i++) {
			personsArrayGlb[i] = new Person(this);
		}


		background(0);

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
	Range colorPeriodGlb;
	Range talkingPeriodGlb;
	float colorChangeSpeedGlb;
	float periodChangeSpeedGlb;

	// Pin communication parameters
	Slider irEmmitingAngleGlb;
	Slider irEmmitingPowerGlb;
	int talkingTimeGlb;
	int releaseTimeGlb;

	void setupControls() {

		int row = 200;
		int col = 20;
		int inc = 20;
		int l = 200;
		int h = 12;

		irEmmitingAngleGlb = controlP5.addSlider("IR Emmiting Angle", 0, 180, 45, col, row += inc, l, h);
		irEmmitingPowerGlb = controlP5.addSlider("IR Emmiting Power", 0, 50, 10, col, row += inc, l, h);
	}

	
	public void controlEvent(ControlEvent theControlEvent) {
		if (theControlEvent.label() == "IR Emmiting Angle") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin._alfa = irEmmitingAngleGlb.value() / 180f;
			}
		}
		if (theControlEvent.label() == "IR Emmiting Power") {
			for (int i = 0; i < personsArrayGlb.length; i++) {
				personsArrayGlb[i]._pin._range = irEmmitingPowerGlb.value() / 100f;
			}
		}
	}

	
	String stats() {
		float meanPeriod = 0;
		float meanColor = 0;
		float sdevPeriod = 0;
		float sdevColor = 0;
		for (int i = 0; i < NUM_PERSONS; i++) {
			meanColor += personsArrayGlb[i]._pin._targetColor;
			meanPeriod += personsArrayGlb[i]._pin._targetColorPeriod;
		}
		meanColor /= NUM_PERSONS;
		meanPeriod /= NUM_PERSONS;

		for (int i = 0; i < NUM_PERSONS; i++) {
			sdevColor += sq(personsArrayGlb[i]._pin._targetColor - meanColor);
			sdevPeriod += sq(personsArrayGlb[i]._pin._targetColorPeriod
					- meanPeriod);
		}
		sdevColor /= NUM_PERSONS;
		sdevPeriod /= NUM_PERSONS;
		sdevColor = sqrt(sdevColor);
		sdevPeriod = sqrt(sdevPeriod);

		String res = "Mean Per : " + nf((int) (meanPeriod * 10000), 4)
				+ "\nSdev Per : " + nf((int) (sdevPeriod * 10000), 4)
				+ "\nMean Col : " + nf((int) (meanColor * 10000), 4)
				+ "\nSdev Col : " + nf((int) (sdevColor * 10000), 4)
				+ "\nCol  Num : " + colMsgNumberGlb + "\nPer  Num : "
				+ perMsgNumberGlb + "\nSyn  Num : " + synMsgNumberGlb
				+ "\nMax  Seq : " + maxSeqNumberGlb;

		return res;
	}

	int lastI = 0;
	String stTxt = "";
	boolean bDrawBackg = true;

	public void draw() {
		if (bPauseGlb)
			return;

		if (bDrawBackg)
			background(0);
		// fill(0, 0);
		// rect(0, 0, width, height);

		for (int i = 0; i < NUM_PERSONS; i++) {
			personsArrayGlb[i].update();
			// personsArrayGlb[i].draw();
			personsArrayGlb[i]._pin.update();
			personsArrayGlb[i]._pin.draw();
			if (mousePressed) {
				if (dist(mouseX, mouseY, width * personsArrayGlb[i]._pin._x,
						height * personsArrayGlb[i]._pin._y) < personsArrayGlb[i]._pin._r
						* width) {
					String txt = "Per : "
							+ nf((int) (personsArrayGlb[i]._pin._colorPeriod * 100),
									2)
							+ "\nCap : "
							+ nf((int) (personsArrayGlb[i]._pin._capacitor * 100),
									2)
							+ "\nCol : "
							+ nf((int) (personsArrayGlb[i]._pin._color * 100),
									2) + "\nMsg : "
							+ personsArrayGlb[i]._pin._lastReceivedMsg;
					text(txt, mouseX, mouseY);
					lastI = i;
					break;
				}
			} else {
				if (i == lastI) {
					text(i, mouseX, mouseY - 10);
					personsArrayGlb[i]._expression.draw(mouseX, mouseY);
				}
			}
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
			bDrawBackg = !bDrawBackg;
			break;
		}
	}

	static public void main(String args[]) {
		PApplet.main(new String[] { "--bgcolor=#F0F0F0",
				"art.petaka.pincells.Simulation" });
	}

}
