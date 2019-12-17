package net.devtech;

import static java.lang.Math.*;

public class Math {
	public static void main(String[] args) {
		for (int i = 30; i < 60; i++) {
			double yvel = sin(toRadians(i))*100;
			int time = 0;
			for (int i1 = 1; i1 < 100; i1++) {
			//	System.out.print(findDistanceTraveled(yvel, i1) + " ");
				if(fuzzyEquals(findDistanceTraveled(yvel, i1), 0)) {
					time = i1;
					break;
				}
			}

			System.out.println(i + " " + time*cos(toRadians(i))*10);
		}
	}


	public static double findVelocityAfterRecursive(double init, int time) {
		if (time == 0) return init;
		return findVelocityAfterRecursive(init * .98 - .0784, --time);
	}

	public static double findVelocityAfter(double init, int time) {
		return (3.92 + init) * pow(.98, time) - 3.92;
	}

	public static double findInitialVelocity(int time, double after) {
		return (after / (pow(.98, time) - 3.92)) - 3.92;
	}

	public static double findDistanceTraveled(double initVel, double time) {
		//return initVel*time - 4.9*time*time;
		if (time < 1) return 0;
		return initVel + findDistanceTraveled((initVel - .08D) * .98, time - 1);
	}

	private static boolean fuzzyEquals(double a, double b) {
		return a == b || abs(a - b) < 20;
	}

}
