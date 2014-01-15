package jsbabel.entities;

public enum StringType {
	Text(0),
	Move(1), 
	Image(2),
        Ignore(3);
	
	private StringType(int value) {
    }

	public static StringType valueOf(int i) {
		switch(i)
		{
		case 0: return Text;
		case 1: return Move;
		case 2: return Image;
		default: return Text;
		}
	}

}
