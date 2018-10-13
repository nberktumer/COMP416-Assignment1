import java.util.Scanner;

import follower.Follower;
import master.Master;

public class MainProgram {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);

		boolean isValidType = false;
		int type = -1;

		while (!isValidType) {
			System.out.println("Please select the application type (1-Master, 2-Follower):");
			type = scanner.nextInt();

			if (type == 1 || type == 2)
				isValidType = true;
			else
				System.err.println("Invalid application type. Please select either 1 (Master) or 2 (Follower)");
		}
		
		if(type == 1)
			new Master();
		else if(type == 2)
			new Follower();
		else
			System.err.println("Unexpected application type.");
	}
}
