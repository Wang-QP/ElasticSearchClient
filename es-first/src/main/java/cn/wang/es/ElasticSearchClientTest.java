package cn.wang.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;

public class ElasticSearchClientTest {

    private TransportClient client;

    @Before
    public void init() throws Exception {
        // 创建一个Settings对象，相当于配置信息。主要配置集群信息。
        Settings settings = Settings.builder().put("cluster.name","my-elasticsearch").build();
        // 创建一个客户端Client对象
        client = new  PreBuiltTransportClient(settings);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9301));
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9302));
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9303));
    }

    @After
    public void close() throws Exception {
        // 关闭Client 对象
        client.close();
    }

    @Test
    public void createIndex() throws Exception {
        // 使用Client 对象创建一个索引库
        client.admin().indices().prepareCreate("index_hello")
                // 执行操作
                .get();
    }

    @Test
    public void setMappings() throws Exception {
        //创建一个settings对象
        Settings settings = Settings.builder().put("cluster.name","my-elasticsearch").build();
        //创建一个TransPortClient对象
        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9301))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9302))
                .addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9303));

        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("article")
                        .startObject("properties")
                            .startObject("id")
                                .field("type","long")
                                .field("store",true)
                            .endObject()
                            .startObject("title")
                                .field("type","text")
                                .field("store",true)
                                .field("analyzer","ik_smart")
                            .endObject()
                            .startObject("content")
                                .field("type","text")
                                .field("store",true)
                                .field("analyzer","ik_smart")
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject();


        // 使用client把mapping信息设置到索引库中
        client.admin().indices()
                // 设置要做映射的索引
                .preparePutMapping("index_hello")
                // 设置要做映射的type
                .setType("article")
                // mapping信息，可以是XContentBuilder对象可以是json格式的字符串
                .setSource(builder)
                // 执行操作
                .get();
        // 关闭连接
        client.close();
    }

    @Test
    public void testAddDocument() throws Exception {
        //创建一个文档对象，创建一个json格式的字符串，或者使用XContentBuilder对象
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("id",1)
                    .field("title","标题")
                    .field("content","内容")
                .endObject();
        // 把文档对象添加到索引库
        client.prepareIndex()
                //设置索引名称
                .setIndex("index_hello")
                //设置type
                .setType("article")
                //设置文档的id,如果不设置会自动生成一个id
                .setId("1")
                //设置文档信息
                .setSource(builder)
                //执行操作
                .get();
    }

    @Test
    public void testAddDocument2() throws Exception {
        //创建一个Article对象
        Article article = new Article();
        //设置对象属性
        article.setId(2);
        article.setTitle("使用实体对象转为json格式的字符串");
        article.setContent("存入索引库");
        //把article对象转换成json格式的字符串
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonDocument = objectMapper.writeValueAsString(article);
        System.out.println(jsonDocument);
        //使用client对象把文档写入索引库
        client.prepareIndex("index_hello","article","2")
                .setSource(jsonDocument, XContentType.JSON)
                .get();
    }

    @Test
    public void testAddDocument3() throws Exception {
        for (int i = 3; i < 100; i++) {
            //创建一个Article对象
            Article article = new Article();
            //设置对象属性
            article.setId(i);
            article.setTitle("使用实体对象转为json格式的字符串"+i);
            article.setContent("存入索引库"+i);
            //把article对象转换成json格式的字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = objectMapper.writeValueAsString(article);
            System.out.println(jsonDocument);
            //使用client对象把文档写入索引库
            client.prepareIndex("index_hello","article",""+i)
                    .setSource(jsonDocument, XContentType.JSON)
                    .get();
        }
    }
}
