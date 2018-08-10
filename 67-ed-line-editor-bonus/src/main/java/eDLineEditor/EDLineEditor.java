package eDLineEditor;


import java.io.*;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * The type of all notes, comments and codes are UTF-8.
 * Use command pattern to fulfill the requirements.
 */

/**
 * command:接收到的命令，分为ed和其他命令
 * defaultFilename：所有命令、解析器公用的默认文件名
 * currentLine：共用的当前行
 * State：用于查看是否需要停止或检查的状态
 * saveList：存储每一次的文本改动，用于撤回和对比文本检查是否q需要问号
 * undoCounter：记录改动文本的命令的个数，用于撤回
 * memory：缓存区，以String方式存储每一句话
 * sc：用于传Scanner对象
 * 总体思路：使用CommandPattern的设计模式，EDLineEDitor为命令接收器，Reader负责解析，Command文件内存储需要的Command
 */
public class EDLineEditor {
	private String command;
	static String defaultFilename;
	static int currentLine;
	static State state=null;
	static List<List<String>> saveList;
	static List<Integer> lineList;
	static  int undoCounter=0;
	static List<String> memory;
	static Scanner sc;

	/**
	 * 接收用户控制台的输入，解析命令，根据命令参数做出相应处理。
	 * 不需要任何提示输入，不要输出任何额外的内容。
	 * 输出换行时，使用System.out.println()。或者换行符使用System.getProperty("line.separator")。
	 *
	 * 待测方法为public static void main(String[] args)方法。args不传递参数，所有输入通过命令行进行。
	 * 方便手动运行。
	 *
	 * 说明：可以添加其他类和方法，但不要删除该文件，改动该方法名和参数，不要改动该文件包名和类名
	 */
	public void EdLineEditorStarter() {
		saveList=new ArrayList<>();
		lineList=new ArrayList<>();
		String edCommand = "ed( .*)?";
		sc=new Scanner(System.in);
		undoCounter=0;
		saveList.clear();
		lineList.clear();
		try {
			command = sc.nextLine();

			if (Pattern.matches(edCommand, command) && !command.equals("")) {
				if(command.equals("ed")){
					memory=new ArrayList<String>(){};
					currentLine=0;

					defaultFilename="undefined";
				}
				else{
					String[] edList=command.split(" ");
					File f=new File(edList[1]);
					defaultFilename=edList[1];
					String temp;
					memory=new ArrayList<String>(){};
					BufferedReader bf=new BufferedReader(new FileReader(f));

					while((temp=bf.readLine())!=null){
						memory.add(temp+System.getProperty("line.separator"));
					}
				}saveList.add(memory);
				currentLine=memory.size();
				lineList.add(currentLine);
				state = State.Open;
			}

			Reader reader = new Reader();
			Command cd=new CommandED(command);
			cd.execute();

			while (state != State.Stop && !command.equals("")) {
				state = reader.check(command =sc.nextLine());
				if (state != State.Stop) {
					Command cmd = reader.CommandReader(command);
					cmd.execute();
				}
			}
			sc.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		EDLineEditor ed=new EDLineEditor();
		ed.EdLineEditorStarter();
	}}
