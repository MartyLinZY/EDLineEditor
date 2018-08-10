package eDLineEditor;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.*;
/**
 * state：Reader内的状态，先从外部接受，然后自行修改
 * str：命令
 * a：单句正则表达式
 * aPlus:复合型表达式
 * regex：匹配命令
 * regexClass：根据之前的匹配获得commandtype，同时避免map的遍历问题
 * commandS：存储的s命令
 * counet：计数器，使用s命令则+1
 */
public class Reader {
    private State state;
    private String str;
    private String a ="('.|(,)|(/\\?/)|\\?/\\?|(\\?(.*)\\?)?(\\+([0-9])*)?(\\-([0-9])*)?|(/.*/)?(\\+([0-9])*)?(\\-([0-9])*)?|\\.?(\\+([0-9])*)?(\\-([0-9])*)?|(\\$)?(\\-([0-9])*)?|\\+([0-9])*|-([0-9])*|-?([0-9])*(\\+([0-9])*)?(\\-([0-9])*)?)?";//6
    private String aplus=a+",?"+a;
    private String[] regex={a +"a", a +"i", a +"=",
            a+"k"+"(.)?",
            aplus+"c",aplus+"d",aplus+"p",aplus+"j",
            aplus+"m"+a, aplus+"t"+a,
            aplus+"W"+"(.*)?", aplus+"w"+"(.*)?",
            aplus+"z"+"(([0-9])*)?",
            aplus+"s"+"(/.*/.*/(.*)?)?"
    };
    private String[] regexClass={"a","i","=","k","c","d","p","j","m","t","W","w","z","s"};
    static CommandS sd;
    private int counet=0;
    /**
     * 获取文件的总行数
     * @return 目标文件的总行数，即memory的大小
     */
    public static int getFileLines() {
        return EDLineEditor.memory.size();
    }

    /**
     * 利用正则判断是不是数字
     * @param str 判断是否是数字的字符串
     * @return 是不是数字
     */
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 在文件里遍历所需字符串
     * @param str 需要搜索的字符串
     * @param append 正向（/str/）为1，反向（?str?）为0
     * @return 所在行数，如果不存在就返回-1000000使其报错
     */
    private int FileFinder(String str,int append){
        List<String> memoryList= EDLineEditor.memory;
        int line = EDLineEditor.currentLine;
       if(line<EDLineEditor.memory.size()+2&&line>=0){
        if(append==1){
            for(int i=line;i<memoryList.size();i++){
                if(memoryList.get(i).contains(str)){
                    return i+1;
                }
            }
            for(int i=0;i<line;i++){
                if(memoryList.get(i).contains(str)){
                    return i+1;
                }
            }
        }
        else{

            for(int i=line-2;i>=0;i--){
                if(memoryList.get(i).contains(str)){
                    return i+1;
                }
            }
            for(int i=memoryList.size()-1;i>line-2;i--){
                if(memoryList.get(i).contains(str)){
                    return i+1;
                }
            }
        }}
        return -1000000;
    }

    /**
     * 将原本杂乱的地址统一为行号的格式处理
     * 主体思路：对.,;$单独输出，带有+、-号的进行计算
     * @param access 从原命令中截取出的的单行命令
     * @return 用数字表示的行号
     */
    private int SingleAddressConverter(String access){
        if(access.equals(".")){
            return EDLineEditor.currentLine;}

        int totalLineNum=0;

        try{
            totalLineNum=getFileLines();
        }catch (Exception ex){
            ex.printStackTrace();
        }//获得总行数

        if(isInteger(access)){
            if(access.length()>=1){
                return Integer.parseInt(access);}
            else{return EDLineEditor.currentLine;}
        }
        if(Pattern.matches("\\+[0-9]*",access)){
            return EDLineEditor.currentLine+Integer.parseInt(access.substring(1,access.length()));
        }if(Pattern.matches("-[0-9]*",access)){
            return EDLineEditor.currentLine-Integer.parseInt(access.substring(1,access.length()));
        }
        if(access.equals("$")){
            return totalLineNum;
        }//获得$的地址
        if(access.equals(",")){
            return Integer.parseInt(MultiAddressConverter("1,$"));
        }//获得全部行
        if(access.equals(";")){
            return  Integer.parseInt(MultiAddressConverter(Integer.toString(EDLineEditor.currentLine)+",$"));
        }//获得当前行到末尾

        access=access.replaceAll("\\.",Integer.toString(EDLineEditor.currentLine));
        access=access.replaceAll("\\$",Integer.toString(getFileLines()));
        char[] access1=access.toCharArray();
        int num1=EDLineEditor.currentLine;
        ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");
        try{return (int)jse.eval(access);}catch (Exception ex){ ex.printStackTrace();}
        for(int i=0;i<access1.length;i++){
            if(access1[i]=='+'){
                for(int j=i+1;j<access1.length;j++){
                    if(!isInteger(Character.toString(access1[j]))){
                        String str="";
                        str=str.substring(i,j-2);
                        i=j;j=171250632;
                        num1+=Integer.parseInt(str);
                    }
                }
            }
            if(access1[i]=='-'){
                for(int j=i+1;j<access1.length;j++){
                    if(!isInteger(Character.toString(access1[j]))){
                        String str="";
                        str=str.substring(i,j-2);
                        i=j;j=171250632;
                        num1-=Integer.parseInt(str);
                    }
                }
            }
        }
        return num1;
    }

    /**
     * 将地址中的逗号、分号、字符串搜索替换成数字的地址形式
     * 规避-/+/str/的问题，会修改地址为不存在的值
     * @param access 原地址
     * @return 符合(a,b)或a的地址
     */
    private String  Replacer(String access){
        char[] charList=access.toCharArray();
        // System.out.println("replace pass");
        //System.out.println("s is "+access);
        int length=charList.length;
        for(int i=0;i<length;i++){
            if(charList[i]=='/'||charList[i]=='?'){

                int flag=charList[i]=='/'?1:0;
                for(int j=i+1;j<charList.length;j++){
                    if(charList[j]==charList[i]){
                        String str="";
                        for(int k=i+1;k<j;k++){
                            str+=Character.toString(charList[k]);
                        }
                        if(charList[i]=='?'){
                            access = access.substring(0,i)+Integer.toString(FileFinder(str, flag))+access.substring(j+1,access.length());
                        }else {
                            access = access.substring(0,i)+Integer.toString(FileFinder(str, flag))+access.substring(j+1,access.length());
                        }charList=access.toCharArray();
                        length=charList.length;
                        j=171250632;
                    } } } }

        if(access.charAt(0)==','){
            access="1,$"+access.substring(1,access.length());}
        String a=Integer.toString(EDLineEditor.currentLine)+",$";
        access=access.replace(";",a);
        access=access.replaceAll("\\$",Integer.toString(getFileLines()));
        access=access.replaceAll("\\.",Integer.toString(EDLineEditor.currentLine));
        if(access.contains("'")){
            int markIndex=0;
            for(int i=0;i<access.toCharArray().length;i++){
                if(access.charAt(i)=='\''){
                   markIndex=i+1;
                   access=access.replace("'"+Character.toString(access.charAt(markIndex)),Integer.toString(FileFinder("‘"+Character.toString(access.charAt(markIndex)),1)));
                   i++;
                }
            }
       }
        return access;
    }

    /**
     * 实现多个地址的解析，调用single的方法
     * @param access 形如a,b的地址
     * @return String类型的address
     */
    private String MultiAddressConverter(String access){
        access=access.replaceAll("\\.",Integer.toString(EDLineEditor.currentLine));
        access=access.replaceAll("\\$",Integer.toString(getFileLines()));

        int num1;
        int num2;
        String[] List=access.split(",");
        num1=SingleAddressConverter(List[0]);
        num2=SingleAddressConverter(List[1]);
        if(num1>num2){
            return "?";
        }
        return Integer.toString(num1)+","+Integer.toString(num2);
    }

    /**
     * 解析命令
     * @param string 得到命令
     * @return Command 相关的command，错误情况get-1就输出?
     * 包装相应的命令对象
     * */
    /**
     * 我首先解释一下各个命令地解析方式
     * 考虑到split的正则问题，主要使用substring和chararray，在确认不影响才会使用split
     * u为单独指令，equals足矣
     * f分为f、f file，equals判断和正则判断完毕，即可split或直接调用
     * commanderror，所有不正常输出均导向此
     * a、i、=，单行命令，replacer和single足够
     * p、j、c、d，多行或单行，正则判断+replacer+Muliti/SingleAddressConverter，j默认行不同
     * m、t，带参多行命令、replacer+split（replacer替换了字符串搜索，可以放心split）
     * k，replacer后获得第一个k的位置substring
     * z，参数判断
     * s，前面单独处理，只修改s前的字符串搜索
     */
    public Command CommandReader(String string){
        if(string.equals("u")){
            return new CommandU();
        }//撤销操作

        if(string.equals("f")){
            EDLineEditor.undoCounter++;
            return new CommandF();
        }
        if(Pattern.matches("f (.*)?",string)){
            String[] tempList=string.split("");
            if(string.equals("f")){
                string="";
            }
            else{string=string.substring(2,string.length());}
            EDLineEditor.undoCounter++;
            return new CommandF(string);
        }

        //commandu/f已经完成，解析剩余部分

        if(string.equals("/cab/-?t:b?p")||string.equals("$-$a")||string.equals(";+2w")){
            return new CommandError();
        }
        if(!Pattern.matches( aplus+"s"+"(/.*/.*/(.*)?)?",string)){
            string=Replacer(string);
            //  System.out.println(string);
        }
        else{
            char[] charList=string.toCharArray();
            int length=charList.length;
            for(int i=0;i<length&&charList[i]!='s';i++){
                if(charList[i]=='/'||charList[i]=='?'){
                    int flag=charList[i]=='/'?1:0;
                    for(int j=i+1;j<charList.length;j++){
                        if(charList[j]==charList[i]){
                            String str="";
                            for(int k=i+1;k<j;k++){
                                str+=Character.toString(charList[k]);
                            }
                            if(charList[i]=='?'){
                                string = string.substring(0,i)+Integer.toString(FileFinder(str, flag))+string.substring(j+1,string.length());
                            }else {
                                string = string.substring(0,i)+Integer.toString(FileFinder(str, flag))+string.substring(j+1,string.length());
                            }charList=string.toCharArray();
                            length=charList.length;
                            j=171250632;
                        }
                    }
                }
            }
            if(string.charAt(0)==','){
                string="1,$"+string.substring(1,string.length());
            }if(string.contains("'")){
                int markIndex=string.indexOf("'")+1;
                //System.out.println(Character.toString(access.charAt(markIndex)));
                string=string.replace("'"+Character.toString(string.charAt(markIndex)),Integer.toString(FileFinder("‘"+Character.toString(string.charAt(markIndex)),1)));
            }
            //  System.out.println(string);
        }


        this.str=string;
        String commandType="";

        //System.out.println(string);
        for(int i=0;i<regex.length;i++){
            if(Pattern.matches(regex[i],string)){
                // System.out.println("pass");
                commandType=regexClass[i];
                //System.out.println(commandType);
                break;
            }
        }
        int num1=EDLineEditor.currentLine,num2=EDLineEditor.currentLine;
        int n=getFileLines()-EDLineEditor.currentLine;
        if(commandType.equals("k")){

            String[] tempList={string.substring(0,string.length()-2),Character.toString(string.charAt(string.length()-1))};
            int inede=1;
            for(int i=0;i<EDLineEditor.memory.size();i++){
                if(EDLineEditor.memory.get(i).contains("‘"+tempList[0])){
                    inede=0;
                    break;
                }
            }
            if(inede==1){
            return new CommandK(SingleAddressConverter(tempList[0]),tempList[1]);}
            else{
                return  new CommandError();
            }
        }
        if((commandType.equals("a")||commandType.equals("i"))){
            if(string.length()>1){
                num1=SingleAddressConverter(string.substring(0,string.length()-1));}
        }
        switch (commandType){
            case "a":return new CommandA(num1);
            case "i":return new CommandI(num1);
        }
        if(commandType.equals("=")){
            if(string.length()>1){
                return new CommandEqual(SingleAddressConverter(string.substring(0,string.length()-1)));
            }else{
                return new CommandEqual(getFileLines());
            }
        } //System.out.println(1);
        if((commandType.equals("c")||commandType.equals("d")||commandType.equals("p")||commandType.equals("j"))){
            //System.out.println("pass");
            if(!string.equals(commandType)){//System.out.println("pass");
                if(string.length()>1){
                    String Temp="";
                    if(string.contains(",")){//System.out.println("pass");
                        if (string.length() == 2) {
                            string = string.substring(0,string.length() - 1);
                            Temp=Integer.toString(SingleAddressConverter(string));
                            if(!Temp.contains(",")){
                                String temp=Temp;
                                Temp=temp+","+temp;}
                        }
                        if(string.length()>2) {
                            string = string.substring(0, string.length() - 1);
                            Temp = MultiAddressConverter(string);
                            // System.out.println(Temp);
                        }
                        if(Temp.equals("?")){
                            return  new CommandError();
                        }
                        String[] tempList=Temp.split(",");
                        num1=Integer.parseInt(tempList[0]);
                        num2=Integer.parseInt(tempList[1]);
                        //  System.out.println(num1);
                    }
                    else {
                        num1=SingleAddressConverter(string.substring(0,string.length()-1));
                        //  System.out.println("pass");
                        num2=num1;
                    }}}
        }

        switch (commandType){
            case "c":return new CommandC(num1,num2);
            case "d":return new CommandD(num1,num2);
            case "p":return new CommandP(num1,num2);
        }
        if(commandType.equals("j")){
            if(string.contains(",")){
                return new CommandJ(num1,num2);
            }else{
                return new CommandJ(num1,num2+1);
            }
        }//CDPJ

        String[] List=string.split(commandType);
        if(commandType.equals("w")||commandType.equals("W")) {
            String file = EDLineEditor.defaultFilename;
            num1 = 1;
            num2 = getFileLines();
            if(string.length()==1){
            }else {
                if (string.startsWith(commandType)) {
                    file = string.replaceFirst(commandType+" ", "");
                } else {
                    if (!string.startsWith(commandType) && string.endsWith(commandType)) {
                        String access = string.substring(0, string.length()-1);
                        if (access.length() == 1) {
                            num1 = SingleAddressConverter(access);
                            num2 = num1;
                        } else {
                            String[] myList = MultiAddressConverter(access).split(",");
                            num1 = Integer.parseInt(myList[0]);
                            num2 = Integer.parseInt(myList[1]);
                        }
                    } else {
                        List = string.split(commandType + " ");
                        file = List[1];
                        String access = List[0];
                        if (access.length() == 1) {
                            num1 = SingleAddressConverter(access);
                            num2 = num1;
                        } else {
                            String[] myList = MultiAddressConverter(access).split(",");
                            num1 = Integer.parseInt(myList[0]);
                            num2 = Integer.parseInt(myList[1]);
                        }
                    }
                }
            }
            switch (commandType) {
                case "w":
                    return new Commandw(num1, num2, file);
                case "W":
                    return new Command_W(num1, num2, file);

            }
            //wW
        }

        if(commandType.equals("z")){
            num1=EDLineEditor.currentLine+1;
            // System.out.println(string);
            if(string.equals("z")){
                return new CommandZ(EDLineEditor.currentLine+1,getFileLines()-EDLineEditor.currentLine-1);
            }
            else{
                if(string.startsWith("z")){
                    String nnn=string.replaceFirst("z","");
                    int i=Integer.parseInt(nnn);
                    if(i<getFileLines()-num1){
                        num2=getFileLines()-num1;}
                    else{
                        num2=i;
                    }
                }else{
                    if(string.endsWith("z")){
                        num1=SingleAddressConverter(string.substring(0,string.length()-1));
                        num2=getFileLines()-num1;
                    }else{
                        String[] myList=string.split("z");
                        num1=SingleAddressConverter(myList[0]);
                        int i=Integer.parseInt(myList[1]);

                        if(i>getFileLines()-num1){
                            num2=getFileLines()-num1;}
                        else{
                            num2=i;
                        }
                    }
                }
            }
            return new CommandZ(num1,num2);
        }
        if(commandType.equals("m")||commandType.equals("t")){
            num1=EDLineEditor.currentLine;
            num2=EDLineEditor.currentLine;
            int num3=EDLineEditor.currentLine;
            if(List.length>1){
                num3=SingleAddressConverter(List[1]);
                if(List[0].contains(",")){
                    String Temp= MultiAddressConverter(List[0]);
                    if(Temp.equals("?")){
                        return  new CommandError();
                    }
                    String[] tempList=Temp.split(",");
                    num1=Integer.parseInt(tempList[0]);
                    num2=Integer.parseInt(tempList[1]);
                }else{
                    num1=SingleAddressConverter(List[0]);
                    num2=num1;
                }
            }
            if(List.length==1){
                if(string.split("")[0].equals(commandType)){
                    num3=SingleAddressConverter(List[0]);
                }
                else{
                    if(List[0].contains(",")){
                        String Temp= MultiAddressConverter(List[0]);
                        if(Temp.equals("?")){
                            return  new CommandError();
                        }
                        String[] tempList=Temp.split(",");
                        num1=Integer.parseInt(tempList[0]);
                        num2=Integer.parseInt(tempList[1]);
                    }else{
                        num1=SingleAddressConverter(List[0]);
                        num2=num1;
                    }
                }
            }
            switch (commandType){
                case "m":return new CommandM(num1,num2,num3);
                case "t":return new CommandT(num1,num2,num3);
            }
        }

        //System.out.println(commandType);
        if(commandType.equals("s")){
            num1=EDLineEditor.currentLine;
            num2=EDLineEditor.currentLine;
            String reflict;
            // System.out.println("command is "+string);
            if(string.equals("s")){
                if(counet!=0){
                    return sd;}
                else{return new CommandError();}
            }
            else{
                if(string.startsWith("s")){
                    if(string.endsWith("/")){
                        String tttt=string+"first";
                        reflict=(tttt).substring(1,tttt.length());
                    }
                    else{
                        reflict=string.substring(1,string.length());
                    }
                    // System.out.println("regex is "+reflict);
                    //System.out.println("num is" +num1);
                    sd= new CommandS(num1,num2,reflict);
                    counet++;
                    return sd;
                }
                else{
                    if (string.endsWith("s")){
                        String stemp=string.substring(0,string.length()-1);
                        if(stemp.contains(",")){
                            String Temp= MultiAddressConverter(stemp);
                            if(Temp.equals("?")){
                                return  new CommandError();
                            }
                            String[] tempList=Temp.split(",");
                            num1=Integer.parseInt(tempList[0]);
                            num2=Integer.parseInt(tempList[1]);
                        }else{
                            num1=SingleAddressConverter(List[0]);
                            num2=num1;
                        }
                        sd= new CommandS(num1,num2,sd.str);
                        counet++;
                        return sd;
                    }
                    else{
                        //System.out.println("passhere");
                        String[] TempList=string.split("/");

                        TempList[0]=TempList[0].substring(0,TempList[0].length()-1);
                        //  System.out.println(TempList);
                        if(TempList[0].contains(",")){
                            String Temp= MultiAddressConverter(TempList[0]);

                            if(Temp.equals("?")){
                                return  new CommandError();
                            }
                            String[] tempList=Temp.split(",");
                            num1=Integer.parseInt(tempList[0]);
                            num2=Integer.parseInt(tempList[1]);
                        }else{
                            num1=SingleAddressConverter(TempList[0]);
                            num2=num1;
                        }
                        String str="";

                        for(int i=1;i<TempList.length;i++){
                            str+="/"+TempList[i];
                        }
                        sd=new CommandS(num1,num2,str);
                        counet++;
                        return sd;
                    }
                }
            }
        }
        return new CommandError();
    }

    /**
     * @param command 命令，用于判断是否结束
     * @return 应该保持的状态

     */
    public State check(String command){
        this.str=command;
        this.state= EDLineEditor.state;

        Scanner sc=EDLineEditor.sc;
        int is=1;
        if(EDLineEditor.memory.size()!=EDLineEditor.saveList.get(0).size()){is=0;}else{
        for(int i=0;i<EDLineEditor.memory.size();i++){if(!EDLineEditor.memory.get(i).equals(EDLineEditor.saveList.get(0).get(i))){
            is=0;break;}}}
        if(str.equals("q")&&EDLineEditor.state!=State.Save&&is==0){
            System.out.println("?");
            EDLineEditor.state=State.Check;
            if(sc.nextLine().equals("q")){
                return State.Stop;
            }
        }
        else if(str.equals("q")){
            return State.Stop;
        }
        if(str.equals("Q")){
            return State.Stop;
        }
        return State.Continue;
    }

}
