package joe.pachong;
import java.text.SimpleDateFormat;
import java.sql.*;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Date;

import javax.print.attribute.standard.PrinterLocation;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.mysql.fabric.xmlrpc.base.Data;

public class NewsCrawler extends BreadthCrawler {

    /**
     * @param crawlPath crawlPath is the path of the directory which maintains
     * information of this crawler
     * @param autoParse if autoParse is true,BreadthCrawler will auto extract
     * links which match regex rules from pag
     */
	String post_title,post_content,post_excerpt,to_ping,pinged,post_content_filtered;
	int post_author;
	String post_date;
	Robot r;//延时避免服务器503
	
    public NewsCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
        /*种子页面*/
        this.addSeed("http://www.qiushibaike.com");

        /*正则规则设置*/
        /*爬取符合 http://news.hfut.edu.cn/show-xxxxxxhtml的URL*/
        this.addRegex("^http://www.qiushibaike.com/8hr/page/.*");
        /*不要爬取 jpg|png|gif*/
        this.addRegex("-.*\\.(jpg|png|gif).*");
        /*不要爬取包含 # 的URL*/
        this.addRegex("-.*#.*");
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
    	
    	
    	String url = page.getUrl();
        /*判断是否为新闻页，通过正则可以轻松判断*/
        if (page.matchUrl("^http://www.qiushibaike.com/8hr/page/.*")) {
            /*we use jsoup to parse page*/
        	//Document doc = page.getDoc();

            /*extract title and content of news by css selector*/
            
        	Elements titles = page.select("h2");
            Elements contents = page.select("div.content");
            String[] titlesArr = new String[20];
            String[] contentsArr = new String[20];
            System.out.println("URL:\n" + url);
            int i = 0, j = 0;
            for(Element title : titles){
            	titlesArr[i] = title.text();
            	i++;
            }
            for(Element content : contents){
            	contentsArr[j] = content.text();
            	j++;
            }

    	
    	Connection conn = null;
	    String sql;
	    // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
	    // 避免中文乱码要指定useUnicode和characterEncoding
	    // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
	    // 下面语句之前就要先创建javademo数据库
	    
	    String sqlUrl = "jdbc:mysql://123.207.163.217:3306/wordpress?"
	            + "user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
	    
	    try{
	    	Class.forName("com.mysql.jdbc.Driver");
	    	System.out.println("成功加载MySQL驱动程序");
            // 一个Connection代表一个数据库连接
            conn = DriverManager.getConnection(sqlUrl);
            System.out.println("sql连接成功");            
            post_author = 1;
            post_excerpt = " ";
            to_ping = " ";
            pinged = " ";
            post_content_filtered = " ";
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            post_date = df.format(new Date());
            Statement stmt = conn.createStatement();
            
            
            //将内容写入数据库
            for(i = 0;i < 20; i++){
            	
            	post_title = titlesArr[i];
            	post_content = contentsArr[i];
            	sql = "insert into wp_posts(post_date,post_title,post_author,post_content,"
            			+ "post_excerpt,to_ping,pinged,post_content_filtered) "
                		+ "values('" + post_date + "','" + post_title + "','" + post_author + "','" 
            			+ post_content + "','" + post_excerpt + "','" 
                		+ to_ping + "','" + pinged + "','" + post_content_filtered + "')";
            	int result = stmt.executeUpdate(sql);// executeUpdate语句会返回一个受影响的行数，如果返回-1就没有成功
                if(result != -1){
                	System.out.println("插入成功" + i );
                }
            }
            // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
		    }catch (SQLException e) {
	            System.out.println("MySQL操作错误");
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
					conn.close();
					System.out.println("sql连接断开");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
    	
            
            //延时避免服务器503
			try {
				r = new Robot();
	            r.delay(1000);
			} catch (AWTException e) {
				e.printStackTrace();
			}   
                 

            /*如果你想添加新的爬取任务，可以向next中添加爬取任务，
               这就是上文中提到的手动解析*/
            /*WebCollector会自动去掉重复的任务(通过任务的key，默认是URL)，
              因此在编写爬虫时不需要考虑去重问题，加入重复的URL不会导致重复爬取*/
            /*如果autoParse是true(构造函数的第二个参数)，爬虫会自动抽取网页中符合正则规则的URL，
              作为后续任务，当然，爬虫会去掉重复的URL，不会爬取历史中爬取过的URL。
              autoParse为true即开启自动解析机制*/
            //next.add("http://xxxxxx.com");
        }
    }

    public static void main(String[] args) throws Exception {
        NewsCrawler crawler = new NewsCrawler("crawl", true);
        /*线程数*/
        crawler.setThreads(1);
        /*设置每次迭代中爬取数量的上限*/
        crawler.setTopN(5000);
        /*设置是否为断点爬取，如果设置为false，任务启动前会清空历史数据。
           如果设置为true，会在已有crawlPath(构造函数的第一个参数)的基础上继
           续爬取。对于耗时较长的任务，很可能需要中途中断爬虫，也有可能遇到
           死机、断电等异常情况，使用断点爬取模式，可以保证爬虫不受这些因素
           的影响，爬虫可以在人为中断、死机、断电等情况出现后，继续以前的任务
           进行爬取。断点爬取默认为false*/
        crawler.setResumable(true);
        /*开始深度为4的爬取，这里深度和网站的拓扑结构没有任何关系
            可以将深度理解为迭代次数，往往迭代次数越多，爬取的数据越多*/
        crawler.start(2);
    }

}