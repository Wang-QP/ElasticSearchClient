package cn.wang.es;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

public class SearchIndexTest {
    private TransportClient client;

    @Before
    public void init() throws Exception {
        // 创建一个Settings对象，相当于配置信息。主要配置集群信息。
        Settings settings = Settings.builder().put("cluster.name","my-elasticsearch").build();
        // 创建一个客户端Client对象
        client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9301));
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9302));
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("127.0.0.1"),9303));
    }

    @After
    public void close() throws Exception {
        // 关闭Client 对象
        client.close();
    }

    private void seaech(QueryBuilder queryBuilder) throws Exception {
        // 执行查询
        SearchResponse searchResponse = client.prepareSearch("index_hello")
                .setTypes("article")
                .setQuery(queryBuilder)
                // 设置分页信息
                .setFrom(1)
                // 设置每页条数
                .setSize(5)
                .get();
        // 取查询结果
        SearchHits hits = searchResponse.getHits();
        // 取查询结果的总记录数
        System.out.println("总数："+hits.getTotalHits());
        // 查询结果列表
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()) {
            SearchHit hit = iterator.next();
            // 打印文档对象，以json格式输出
            System.out.println(hit.getSourceAsString());
            // 取文档的属性
            System.out.println("----------文档的属性");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap.get("id"));
            System.out.println(sourceAsMap.get("title"));
            System.out.println(sourceAsMap.get("content"));
        }
    }

    @Test
    public void searchByIdTest() throws Exception {
        // 创建一个查询对象
        QueryBuilder queryBuilder = QueryBuilders.idsQuery().addIds("1","2");
        seaech(queryBuilder);
    }

    @Test
    public void queryByTerm() throws Exception {
        // 创建一个QueryBuilder对象
        // 参数1：要搜索的字段
        // 参数2：要搜索的关键词
        QueryBuilder queryBuilder = QueryBuilders.termQuery("title","格式");
        // 执行查询
        seaech(queryBuilder);
    }

    @Test
    public void queryStringQueryTest() throws Exception {
        // 创建一个QueryBuilder对象
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("一句话的格式")
                .defaultField("title");
        // 执行查询
        seaech(queryBuilder,"title");
    }


    // 高亮
    private void seaech(QueryBuilder queryBuilder, String highlightField) throws Exception {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        // 设置高亮
        highlightBuilder.field(highlightField);
        highlightBuilder.preTags("<em>");
        highlightBuilder.postTags("</em>");
        // 执行查询
        SearchResponse searchResponse = client.prepareSearch("index_hello")
                .setTypes("article")
                .setQuery(queryBuilder)
                // 设置分页信息
                .setFrom(1)
                // 设置每页条数
                .setSize(5)
                .highlighter(highlightBuilder)
                .get();
        // 取查询结果
        SearchHits hits = searchResponse.getHits();
        // 取查询结果的总记录数
        System.out.println("总数："+hits.getTotalHits());
        // 查询结果列表
        Iterator<SearchHit> iterator = hits.iterator();
        while (iterator.hasNext()) {
            SearchHit hit = iterator.next();
            // 打印文档对象，以json格式输出
            System.out.println(hit.getSourceAsString());
            // 取文档的属性
            System.out.println("----------文档的属性");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            System.out.println(sourceAsMap.get("id"));
            System.out.println(sourceAsMap.get("title"));
            System.out.println(sourceAsMap.get("content"));

            System.out.println("**************高亮结果");
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            System.out.println(highlightFields);
            HighlightField field = highlightFields.get(highlightField);
            Text[] fragments = field.getFragments();
            if (fragments != null) {
                String title = fragments[0].toString();
                System.out.println(title);
            }
            System.out.println("======================");
        }
    }
}
