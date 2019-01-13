package eDLineEditor;

import javax.swing.text.EditorKit;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * 接口类
 * 保证所有command可以统一命名且一定可以执行
 */
public interface Command { public void execute();}
/**
 * 初始化需要行号
 * 执行：新建列表，在相应位置接受输入，然后将列表对象传回memory
 */
class CommandA implements Command{
    private int Linenum;
    private final int  defaultAddress=SaveArea.currentLine;
    public CommandA(int access) {
        if(access==-1){
            Linenum=defaultAddress;
        }else {
            Linenum = access;
        }
    }
    @Override
    public void execute() {
        try {
            Scanner sc=SaveArea.sc;
            String nextLine;

            List<String> temp=new ArrayList<>();
            if(Linenum<0||Linenum>SaveArea.memory.size()){System.out.println("?");}
            else {
                SaveArea.currentLine=Linenum;
                for (int i = 0; i < Linenum; i++) {
                    temp.add(SaveArea.memory.get(i));
                }
                while (!(nextLine = sc.nextLine()).equals(".")) {
                    temp.add(nextLine + System.getProperty("line.separator"));
                    SaveArea.currentLine++;
                }
                for (int i = Linenum; i < SaveArea.memory.size(); i++) {
                    temp.add(SaveArea.memory.get(i));
                }
                SaveArea.memory = temp;
                SaveArea.saveList.add(SaveArea.memory);
                SaveArea.lineList.add(SaveArea.currentLine);
                SaveArea.undoCounter++;
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
/**
 * 初始化需要起始行和结束行
 * 对缓存区操作增删改查
 */
class CommandC implements Command{

    private int num1,num2;
    public CommandC(int access1,int access2) {
        num1=access1;
        num2=access2;
    }
    @Override
    public void execute() {
        try {
            if (num1 <= 0||num2>SaveArea.memory.size()) {
                System.out.println("?");
            } else {
                List<String> temp = new ArrayList<>();
                Scanner sc = SaveArea.sc;
                for (int i = 0; i < num1 - 1; i++) {
                    temp.add(SaveArea.memory.get(i));
                }
                SaveArea.currentLine = num1 - 1;
                String str = "";
                while ((str = sc.nextLine()) != null && !str.equals(".")) {
                    temp.add(str + System.getProperty("line.separator"));
                    SaveArea.currentLine++;
                }
                for (int i = num2; i < SaveArea.memory.size(); i++) {
                    temp.add(SaveArea.memory.get(i));
                }
                SaveArea.memory = temp;
                SaveArea.saveList.add(SaveArea.memory);
                SaveArea.lineList.add(SaveArea.currentLine);
                SaveArea.undoCounter++;
            }

        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
/**
 * 不需要参数初始化
 * 直接输出问号
 */
class CommandError implements Command{
    @Override
    public void execute() {
        System.out.println("?");
    }}
/**
 * 初始化需要起始行和结束行
 * 对缓存区操作增删改查
 */
class CommandD implements Command{
    private int num1,num2;
    public CommandD(int access1,int access2) {
        num1=access1;
        num2=access2;
    }
    @Override
    public void execute() {
        try {
            int length=SaveArea.memory.size();
            List<String> temp=new ArrayList<>();
            if(num1>SaveArea.memory.size()||num2<=0||num1>num2||num1<=0){System.out.println("?");}else{
                for(int i=0;i<length;i++){
                    if(i<num1-1||i>num2-1){
                        temp.add(SaveArea.memory.get(i));
                    }
                }
                SaveArea.memory=temp;
                if(num2>=length){
                    SaveArea.currentLine=num1-1;
                }else{
                    SaveArea.currentLine=num1;
                }}
            SaveArea.saveList.add(SaveArea.memory);
            SaveArea.lineList.add(SaveArea.currentLine);
            SaveArea.undoCounter++;
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
/**
 * 初始化需要行号
 * 执行：新建列表，在相应位置接受输入，然后将列表对象传回memory
 */
class CommandI implements Command{
    private int Linenum;
    public CommandI(int access) {
        Linenum=access;
    }
    @Override
    public void execute() {
        try {
            String nextLine;
            Scanner sc=SaveArea.sc;

            List<String> temp=new ArrayList<>();
            if(Linenum<0||Linenum>SaveArea.memory.size()){System.out.println("?");}
            else{ if(Linenum==0){SaveArea.currentLine=Linenum;}else{
                SaveArea.currentLine=Linenum-1;}
                for(int i=0;i<Linenum-1;i++){
                    temp.add(SaveArea.memory.get(i));
                }
                while(!(nextLine=sc.nextLine()).equals(".")){
                    temp.add(nextLine+System.getProperty("line.separator"));
                    SaveArea.currentLine++;
                }
                if(Linenum>0){
                    for(int i=Linenum-1;i<SaveArea.memory.size();i++){temp.add(SaveArea.memory.get(i));}}
                SaveArea.memory=temp;
                SaveArea.saveList.add(SaveArea.memory);
                SaveArea.lineList.add(SaveArea.currentLine);
                SaveArea.undoCounter++;
            }}
        catch(Exception ex){ex.printStackTrace(); }
    }
}
/**
 * 初始化需要行号
 * 直接输出行号
 */
class CommandEqual implements Command{
    private int Linenum;
    public CommandEqual(int access) {
        Linenum=access;
    }
    @Override
    public void execute() {if(Linenum>=0){
        System.out.println(Linenum);}
        else{System.out.println("?");}
    }
}
/**
 * 撤回
 * 调用上次的文本和当前行
 * 计数器减一
 */
class CommandU implements Command{
    public CommandU() {
    }
    @Override
    public void execute() {
        SaveArea.memory=SaveArea.saveList.get(SaveArea.undoCounter-1);
        SaveArea.currentLine=SaveArea.lineList.get(SaveArea.undoCounter-1);
        SaveArea.undoCounter--;
    }
}
/**
 * 利用bufferedwriter写入相应行，w有追加模式
 */
class  Commandw implements Command{
    private int num1,num2;
    private String filename;
    public Commandw(int access1,int access2,String filename) {
        num1=access1;
        num2=access2;

        this.filename=filename;
    }
    @Override
    public void execute() {
        if(SaveArea.defaultFilename.equals("未命名")&&filename.equals("")){
            System.out.println("?");
        }
        else{
            if(num1<0||num2>SaveArea.memory.size()||num1>num2){System.out.println("?");}else{
                SaveArea.state=State.Save;
                try{ File f=new File(filename);
                    if(!f.exists()){
                        f.createNewFile();
                    }else{
                        f.delete();
                        f.createNewFile();
                    }BufferedWriter bw=new BufferedWriter(new FileWriter(f));
                    for(int i=num1-1;i<num2;i++){
                        if(SaveArea.memory.get(i).contains("‘")){
                            bw.write(SaveArea.memory.get(i).split("‘")[0]+System.getProperty("line.separator"));
                        }else{
                        bw.write(SaveArea.memory.get(i));}
                    }
                    bw.close();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }}}
class Command_W implements Command{
    private int num1,num2;
    private String filename;
    public Command_W(int access1, int access2, String filename) {
        num1=access1;
        num2=access2;
        this.filename=filename;
    }
    @Override
    public void execute() {
        if(SaveArea.defaultFilename.equals("未命名")&&filename.equals("")){
            System.out.println("?");
        }
        else{
            SaveArea.state=State.Save;
            try{ File f=new File(filename);

                if(!f.exists()){

                    f.createNewFile();
                    BufferedWriter bw=new BufferedWriter(new FileWriter(f));
                    for(int i=num1-1;i<num2;i++){
                        if(SaveArea.memory.get(i).contains("‘")){
                            bw.write(SaveArea.memory.get(i).split("‘")[0]+System.getProperty("line.separator"));
                        }else{
                            bw.write(SaveArea.memory.get(i));}
                    }
                    bw.close();
                }else{
                    BufferedWriter bw=new BufferedWriter(new FileWriter(f,true));
                    for(int i=num1-1;i<num2;i++){
                        if(SaveArea.memory.get(i).contains("‘")){
                            bw.write(SaveArea.memory.get(i).split("‘")[0]+System.getProperty("line.separator"));
                        }else{
                            bw.write(SaveArea.memory.get(i));}
                        bw.flush();
                    }
                    bw.close();
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        }
    }
}
/**
 * 初始化需要起始行和结束行
 * 对缓存区操作增删改查
 */
class CommandS implements Command{
    public int num1,num2;
    public String str;

    public CommandS(int num1,int num2,String str) {
        this.num1=num1;
        this.num2=num2;
        this.str=str;
    }
    @Override
    public void execute() {
        try {
            if(str.equals("777777")){
                System.out.println("?");
            }
            else{
                if(num1<=0||num2<=0){System.out.println("?");}
                else{
                    //System.out.println(num1);
                    String[] List=str.split("/");
                    String str1=List[1];
                    String str2;      String num;
                    if(List.length<3){
                        str1=" comman";
                        str2="";
                        num="first";
                    }else{
                    str2=List[2];}

                    if(List.length==4){
                        num=List[3];}
                    else
                    {
                        num="first";
                    }
                    List<String> temp=new ArrayList<>();
                    int flag=0;
                    int ask=0;
                    for(int i=num1-1;i<num2;i++){
                        if(SaveArea.memory.get(i).contains(str1)){
                            flag=1;
                            SaveArea.currentLine=i+1;
                        }
                    }
                    if(flag==0){ ask=0;
                    }else{
                        if(num.equals("g")) {
                            for(int i=0;i<num1-1;i++){
                                temp.add(SaveArea.memory.get(i));
                            }
                            for(int i=num1-1;i<num2;i++){
                                temp.add(SaveArea.memory.get(i).split("‘")[0].replaceAll(str1,str2)+System.getProperty("line.separator"));
                            }
                            for(int i=num2;i<SaveArea.memory.size();i++){
                                temp.add(SaveArea.memory.get(i));
                            }
                            ask=1;
                        }else{
                            if(num.equals("first")){
                                for(int i=0;i<num1-1;i++){
                                    temp.add(SaveArea.memory.get(i));
                                }
                                for(int i=num1-1;i<num2;i++){
                                    temp.add(SaveArea.memory.get(i).split("‘")[0].replaceFirst(str1,str2)+System.getProperty("line.separator"));
                                }
                                for(int i=num2;i<SaveArea.memory.size();i++){
                                    temp.add(SaveArea.memory.get(i));
                                }
                                ask=1;
                            }else{
                                int n=Integer.parseInt(num);
                                for(int i=0;i<num1-1;i++){
                                    temp.add(SaveArea.memory.get(i));
                                }

                                for(int i=num1-1;i<num2;i++){
                                    int count=0;
                                    int startwith=-1;
                                    flag=1;
                                    String tempStr="";
                                    char[] mList= SaveArea.memory.get(i).toCharArray();
                                    for(int j=0;j<SaveArea.memory.get(i).length();j++){
                                        char[] str1List=str1.toCharArray();
                                        if(str1List[0]==mList[j]){
                                            if(count==n-1){
                                                for(int k=0;k<str1.length()&&flag!=0;k++){
                                                    if(mList[j+k]!=str1List[k]){
                                                        flag=0; }
                                                }
                                                if(flag==1){ startwith=j;j=171250632;}flag=1;
                                            }else{for(int k=0;k<str1.length()&&flag!=0;k++){
                                                if(mList[j+k]!=str1List[k]){
                                                    flag=0;
                                                }
                                            }
                                                if(flag==1){count++;}flag=1;
                                            }
                                        }
                                    }
                                    if(startwith!=-1){ask=1;
                                        for(int j=0;j<startwith;j++){
                                            tempStr+=Character.toString(SaveArea.memory.get(i).charAt(j));
                                        }
                                        tempStr+=str2;       SaveArea.currentLine=i+1;
                                        for(int j=startwith+str1.length();j<SaveArea.memory.get(i).length();j++){
                                            tempStr+=Character.toString(SaveArea.memory.get(i).charAt(j));
                                        }
                                        temp.add(tempStr.split("‘")[0]+System.getProperty("line.separator"));
                                    }else{

                                        temp.add(SaveArea.memory.get(i));
                                    }
                                }
                                for(int i=num2;i<SaveArea.memory.size();i++){
                                    temp.add(SaveArea.memory.get(i));
                                }
                            }
                        }

                       SaveArea.memory=temp;SaveArea.saveList.add(SaveArea.memory);
                       SaveArea.lineList.add(SaveArea.currentLine);
                       SaveArea.undoCounter++;
                    }if(ask==0){System.out.println("?");}
                }}

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
/**
 * 可以不需要参数初始化
 * 无参初始化就修改filename为需要打印，否则就修改filename
 * 如果文件名为undefined，且需要输出就报错
 * 否则输出文件名
 * 有参则无反应
 */
class CommandF implements Command{
    private String filename;
    public CommandF() {
        filename="pleaseprintthefilename";
    }
    public CommandF(String args) {
        SaveArea.defaultFilename=args;
        filename=args;
    }
    @Override
    public void execute() {
        if(SaveArea.defaultFilename.equals("undefined")&&filename.equals("pleaseprintthefilename")){
            System.out.println("?");
        }
        else if(filename.equals("pleaseprintthefilename")){
            System.out.println(SaveArea.defaultFilename);
        }

    }
}
    /**
 * 初始化需要起始行和结束行
 * 对缓存区操作增删改查
 */
class CommandJ implements Command{
    private int num1,num2;
    public CommandJ(int access1,int access2) {
        num1=access1;
        num2=access2;
    }
    @Override
    public void execute() {
        try {
            List<String> temp=new ArrayList<>();
            String str="";
            if(num2>SaveArea.memory.size()){System.out.println("?");}
            else {
                for (int i = 0; i < num1 - 1; i++) {
                    temp.add(SaveArea.memory.get(i));
                }
                for (int i = num1 - 1; i < num2 - 1; i++) {
                    if(SaveArea.memory.get(i).contains("‘")){
                        str += SaveArea.memory.get(i).split("‘")[0].replaceAll("\r|\n", "");
                    }else{
                        str += SaveArea.memory.get(i).replaceAll("\r|\n", "");}
                }
                if(SaveArea.memory.get(num2-1).contains("‘")){
                    temp.add(str + SaveArea.memory.get(num2 - 1).split("‘")[0].replaceAll("\r|\n", "")+System.getProperty("line.separator"));
                }
                else{
                temp.add(str + SaveArea.memory.get(num2 - 1));}
                for (int i = num2; i < SaveArea.memory.size(); i++) {
                    temp.add(SaveArea.memory.get(i));
                }
                SaveArea.memory = temp;
                SaveArea.currentLine = num1;
                SaveArea.saveList.add(SaveArea.memory);
                SaveArea.lineList.add(SaveArea.currentLine);
                SaveArea.undoCounter++;
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

}
/**
 * 初始化需要起始行和结束行以及转移行
 * 对缓存区操作增删改查
 */
class CommandM implements Command{
    private int num1, num2, num3;

    public CommandM(int address1, int address2, int address3) {
        num1 = address1;
        num2 = address2;
        num3 = address3;
    }

    @Override
    public void execute() {
        try {
            List<String> temp=new ArrayList<>();
            if (num1 > num3) {
                for(int i=0;i<num3;i++){ temp.add(SaveArea.memory.get(i)); }
                for(int i=num1-1;i<num2;i++){ temp.add(SaveArea.memory.get(i)); }
                for(int i=num3;i<num1-1;i++){temp.add(SaveArea.memory.get(i));}
                for(int i=num2;i<SaveArea.memory.size();i++){temp.add(SaveArea.memory.get(i));}
                SaveArea.currentLine=num3+num2-num1+1;

                SaveArea.memory=temp;
            }
            if (num2 < num3) {
                for(int i=0;i<num1-1;i++){ temp.add(SaveArea.memory.get(i)); }
                for(int i=num2;i<num3;i++){ temp.add(SaveArea.memory.get(i)); }
                for(int i=num1-1;i<num2;i++){temp.add(SaveArea.memory.get(i));}
                for(int i=num3;i<SaveArea.memory.size();i++){temp.add(SaveArea.memory.get(i));}
                SaveArea.currentLine=num3;
                SaveArea.memory=temp;
            }
            if(num1==num2&&num1==num3){
                SaveArea.currentLine=num1;
            }
           SaveArea.saveList.add(SaveArea.memory);
           SaveArea.lineList.add(SaveArea.currentLine);
           SaveArea.undoCounter++;


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

    /**
 * 初始化需要起始行和结束行以及转移行
 * 对缓存区操作增删改查
 */
class CommandT implements Command{
    private int num1,num2,num3;
    public CommandT(int address1,int address2,int address3) {
        num1=address1;
        num2=address2;
        num3=address3;
    }
    @Override
    public void execute() {
        try {
            List<String> temp=new ArrayList<>();
            for(int i=0;i<num3;i++){temp.add(SaveArea.memory.get(i));}
            for(int i=num1-1;i<num2;i++){temp.add(SaveArea.memory.get(i));}
            for(int i=num3;i<SaveArea.memory.size();i++){temp.add(SaveArea .memory.get(i));}
           SaveArea.memory=temp;

           SaveArea.currentLine=num3+num2-num1+1;
           SaveArea.saveList.add(SaveArea.memory);
           SaveArea.lineList.add(SaveArea.currentLine);
           SaveArea.undoCounter++;
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
/**
 * 为标记文本上标记
 * 需要行号和标记
 *会检查是否已有这个标记
 */
class CommandK implements Command{
    private int num;
    private String str;
    public CommandK(int address,String a) {
        num=address;
        str=a;
    }
    @Override
    public void execute() {
        try {
            if(Pattern.matches("[a-z]",str)){
            SaveArea.memory.set(num-1,SaveArea.memory.get(num-1).replaceAll("\r|\n","")+"‘"+str+System.getProperty("line.separator"));
            SaveArea.saveList.add(SaveArea.memory);
            SaveArea.lineList.add(SaveArea.currentLine);
            SaveArea.undoCounter++;}
            else{System.out.println("?");}
        }catch (Exception ex){ex.printStackTrace();}
    }
}
/**
 * 直接调用p
 */
class CommandZ implements Command{
    private int num1;
    private int n;
    public CommandZ(int address,int n) {
        num1=address;
        this.n=n;
    }
    @Override
    public void execute() {
        Command cmd=new CommandP(num1,num1+n);
        cmd.execute();
    }
}
/**
 * 初始化需要起始行和结束行
 * 打印相应内容，如果被标记需要特殊处理
 */
class CommandP implements Command{
    private int num1,num2;
    public CommandP(int address1,int address2) {
        num1=address1;
        num2=address2;
    }
    @Override
    public void execute() {
        try {
            if (num1<0||num2 > SaveArea.memory.size()) {
                System.out.println("?");
            } else {

                SaveArea.currentLine = num2;
                for (int i = num1 - 1; i >= 0 && i <= num2 - 1; i++) {

                    if(SaveArea.memory.get(i).contains("‘")){
                        System.out.print(SaveArea.memory.get(i).split("‘")[0].replaceAll("\r|\n", "")+System.getProperty("line.separator"));
                    }else{
                        System.out.print(SaveArea.memory.get(i));}
                }
            }}
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}


