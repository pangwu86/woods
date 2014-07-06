让excel导入导出更加简单
=======

简单的说做就是完成下面的事情:

					JSON格式配置文件
    数据 <-----------------------------------> Excel文件
	
### 功能列表

目前完成的:

1. List转换为一个Excel的Sheet, 支持设置第一行数据为列名
2. 一个Excel中的Sheet转换为List
3. 使用J4EConf作为配置文件(符合Json格式), 自定义列名, 自定义导入导出的列

准备实现的:

1. 支持@J4EIgnore, 可以过滤字段
2. 支持XSSF模式, 在解析xlsx文件时更加节省内存
3. 支持更复杂的数据格式转换为Excel(格式待定..)
4. 支持数据转换为多个Sheet, 也支持对应的反向操作
5. 支持csv格式

### 接口API
	
本着使用起来最简单的原则, 尽可能的简化了接口

J4E的主要方法:
	
	// Excel转换数据 
	
	/**
	 * 解析输入流, 按照j4eConf中的配置, 读取后返回objClz类型的数量列表
	 * 
	 * @param in
	 *            输入流
	 * @param objClz
	 *            转换后的对象Class, 对应一行数据
	 * @param j4eConf
	 *            转换配置(非必须, 可自动生成)
	 * @return 数据列表
	 */
	List<T> fromExcel(InputStream in, Class<T> objClz, J4EConf j4eConf)
	
	// 数据转换Excel
	
	/**
	 * 将给定的数据列表datalist, 按照j4eConf中的配置, 输出到out
	 * 
	 * @param out
	 *            输出流
	 * @param objClz
	 *            转换后的对象Class, 对应一行数据
	 * @param j4eConf
	 *            转换配置(非必须, 可自动生成)
	 * 
	 * @return 是否转换并写入成功
	 */
	boolean toExcel(OutputStream out, List<?> dataList, J4EConf j4eConf) {
	
J4EConf的主要方法

	// 根据最终要转换的对象的Class, 自动分析字段生成配置
	J4EConf from(Class<?> clz)
	
	// 下面几个是读取JSON配置文件
	J4EConf from(File confFile)
	J4EConf from(String confPath)
	J4EConf from(Reader confReader)
	J4EConf from(InputStream confInputStream)
	
	// 这个是直接读取JSON字符串
	J4EConf fromConf(CharSequence confStr)

当然你可以手动new一个J4EConf对象出来, 然后设置对应属性

下面看一下J4EConf的配置文件是什么样的:

	// 先建立一个类 Person
	
	@J4EName("人员")
	public class Person {

	    @J4EName("姓名")
	    private String name;

	    @J4EName("年龄")
	    private int age;

	    private Date birthday;

	    public String getName() {
	        return name;
	    }
		
		....... // 对应的get, set方法
	}
	
	// 自动生成后的J4EConf(json格式)
	{
	   "sheetName" :"人员",
	   "columns" :[{
	      "fieldName" :"name",
	      "columnIndex" :0,
	      "columnName" :"姓名"
	   }, {
	      "fieldName" :"age",
	      "columnIndex" :1,
	      "columnName" :"年龄"
	   }, {
	      "fieldName" :"birthday",
	      "columnIndex" :2,
	      "columnName" :"birthday"
	   }]
	}
	
在实际项目中, 可以先生成好对应的配置文件保存为json格式文件, 在运行中加载并生成对应Excel

比如导出数据后, 生成Excel文件供用户下载

	List<Person> plist = ...... // 拿到一组数据
	
	J4E.toExcel(out, plitst, J4EConf.from("/export/conf/人员配置.js"));
	
