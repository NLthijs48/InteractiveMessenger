
public class ConverterTest {

	public static void main(String[] args) {
		String in = "";

		in += "Hey there![break]"                          + '\n';
		in += "Welcome to FancyMessageFormat! :)"          + '\n';

		String out = FancyMessageFormatConverter.convertToJSON(in);
		System.out.println(out);

		in = "";
		in += "We are [underline]able[/underline][break]"                                                         + '\n';
		in += "To do some [red][b]really[/b][white] "                                                             + '\n';
		in += "[blue][i]CrAZY[/i] "                                                                               + '\n';
		in += "    hover: Yiiih[yellow]aaaaa!"                                                                    + '\n';
		in += "    hover: [blue]Aiaiaiai[green]aiaiaiai!"                                                         + '\n';
		in += "stuff with your text - "                                                                           + '\n';
		in += "while keeping it quite readable at the same time. [red][b]Awesome,[/b][blue] isn't it?"            + '\n';

		out = FancyMessageFormatConverter.convertToJSON(in);
		System.out.println(out);
	}

}
