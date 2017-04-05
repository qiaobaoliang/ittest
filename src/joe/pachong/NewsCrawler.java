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
	Robot r;//��ʱ���������503
	
    public NewsCrawler(String crawlPath, boolean autoParse) {
        super(crawlPath, autoParse);
        /*����ҳ��*/
        this.addSeed("http://www.qiushibaike.com");

        /*�����������*/
        /*��ȡ���� http://news.hfut.edu.cn/show-xxxxxxhtml��URL*/
        this.addRegex("^http://www.qiushibaike.com/8hr/page/.*");
        /*��Ҫ��ȡ jpg|png|gif*/
        this.addRegex("-.*\\.(jpg|png|gif).*");
        /*��Ҫ��ȡ���� # ��URL*/
        this.addRegex("-.*#.*");
    }

    @Override
    public void visit(Page page, CrawlDatums next) {
    	
    	
    	String url = page.getUrl();
        /*�ж��Ƿ�Ϊ����ҳ��ͨ��������������ж�*/
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
	    // MySQL��JDBC URL��д��ʽ��jdbc:mysql://�������ƣ����Ӷ˿�/���ݿ������?����=ֵ
	    // ������������Ҫָ��useUnicode��characterEncoding
	    // ִ�����ݿ����֮ǰҪ�����ݿ����ϵͳ�ϴ���һ�����ݿ⣬�����Լ�����
	    // �������֮ǰ��Ҫ�ȴ���javademo���ݿ�
	    
	    String sqlUrl = "jdbc:mysql://123.207.163.217:3306/wordpress?"
	            + "user=root&password=123456&useUnicode=true&characterEncoding=UTF8";
	    
	    try{
	    	Class.forName("com.mysql.jdbc.Driver");
	    	System.out.println("�ɹ�����MySQL��������");
            // һ��Connection����һ�����ݿ�����
            conn = DriverManager.getConnection(sqlUrl);
            System.out.println("sql���ӳɹ�");            
            post_author = 1;
            post_excerpt = " ";
            to_ping = " ";
            pinged = " ";
            post_content_filtered = " ";
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            post_date = df.format(new Date());
            Statement stmt = conn.createStatement();
            
            
            //������д�����ݿ�
            for(i = 0;i < 20; i++){
            	
            	post_title = titlesArr[i];
            	post_content = contentsArr[i];
            	sql = "insert into wp_posts(post_date,post_title,post_author,post_content,"
            			+ "post_excerpt,to_ping,pinged,post_content_filtered) "
                		+ "values('" + post_date + "','" + post_title + "','" + post_author + "','" 
            			+ post_content + "','" + post_excerpt + "','" 
                		+ to_ping + "','" + pinged + "','" + post_content_filtered + "')";
            	int result = stmt.executeUpdate(sql);// executeUpdate���᷵��һ����Ӱ����������������-1��û�гɹ�
                if(result != -1){
                	System.out.println("����ɹ�" + i );
                }
            }
            // Statement������кܶ෽��������executeUpdate����ʵ�ֲ��룬���º�ɾ����
		    }catch (SQLException e) {
	            System.out.println("MySQL��������");
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            try {
					conn.close();
					System.out.println("sql���ӶϿ�");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
    	
            
            //��ʱ���������503
			try {
				r = new Robot();
	            r.delay(1000);
			} catch (AWTException e) {
				e.printStackTrace();
			}   
                 

            /*�����������µ���ȡ���񣬿�����next�������ȡ����
               ������������ᵽ���ֶ�����*/
            /*WebCollector���Զ�ȥ���ظ�������(ͨ�������key��Ĭ����URL)��
              ����ڱ�д����ʱ����Ҫ����ȥ�����⣬�����ظ���URL���ᵼ���ظ���ȡ*/
            /*���autoParse��true(���캯���ĵڶ�������)��������Զ���ȡ��ҳ�з�����������URL��
              ��Ϊ�������񣬵�Ȼ�������ȥ���ظ���URL��������ȡ��ʷ����ȡ����URL��
              autoParseΪtrue�������Զ���������*/
            //next.add("http://xxxxxx.com");
        }
    }

    public static void main(String[] args) throws Exception {
        NewsCrawler crawler = new NewsCrawler("crawl", true);
        /*�߳���*/
        crawler.setThreads(1);
        /*����ÿ�ε�������ȡ����������*/
        crawler.setTopN(5000);
        /*�����Ƿ�Ϊ�ϵ���ȡ���������Ϊfalse����������ǰ�������ʷ���ݡ�
           �������Ϊtrue����������crawlPath(���캯���ĵ�һ������)�Ļ����ϼ�
           ����ȡ�����ں�ʱ�ϳ������񣬺ܿ�����Ҫ��;�ж����棬Ҳ�п�������
           �������ϵ���쳣�����ʹ�öϵ���ȡģʽ�����Ա�֤���治����Щ����
           ��Ӱ�죬�����������Ϊ�жϡ��������ϵ��������ֺ󣬼�����ǰ������
           ������ȡ���ϵ���ȡĬ��Ϊfalse*/
        crawler.setResumable(true);
        /*��ʼ���Ϊ4����ȡ��������Ⱥ���վ�����˽ṹû���κι�ϵ
            ���Խ�������Ϊ����������������������Խ�࣬��ȡ������Խ��*/
        crawler.start(2);
    }

}