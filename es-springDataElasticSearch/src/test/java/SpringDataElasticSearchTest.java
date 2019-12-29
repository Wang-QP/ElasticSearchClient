import cn.wang.es.entity.Article;
import cn.wang.es.repositories.ArticleRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class SpringDataElasticSearchTest {
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Test
    public void createIndex() throws Exception {
        // 创建索引，并配置映射关系
        template.createIndex(Article.class);
        // 配置映射关系
//        template.putMapping(Article.class);
    }

    @Test
    public void addDocument() throws Exception {
        for (int i = 0; i < 10; i++) {
            // 创建一个Article对象
            Article article = new Article();
            article.setId(i);
            article.setTitle("标题"+i);
            article.setContent("内容"+i);
            articleRepository.save(article);
        }
    }

    @Test
    public void deleteDocument() throws Exception {
        articleRepository.deleteById(2L);
    }

    @Test
    public void findAll() throws Exception {
        Iterable<Article> articles = articleRepository.findAll();
        articles.forEach(item -> System.out.println(item));
    }

    @Test
    public void findById() throws Exception {
        Optional<Article> optional = articleRepository.findById(3L);
        Article article = optional.get();
        System.out.println(article);
    }

    @Test
    public void findByTitle() throws Exception {
        List<Article> articleList = articleRepository.findByTitle("标题");
        articleList.stream().forEach(item -> System.out.println(item));
    }

    @Test
    public void findByTitleOrContent() throws Exception {
        // 设置分页信息
        Pageable pageable = PageRequest.of(0,15);
        List<Article> list = articleRepository.findAllByTitleOrContent("1", "3", pageable);
        list.stream().forEach(item -> System.out.println(item));
    }

    @Test
    public void testNativeSearchQuery() throws Exception {
        // 创建一个查询对象
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.queryStringQuery("标题内容").defaultField("title"))
                .withPageable(PageRequest.of(0,10)).build();
        // 执行查询
        List<Article> articleList = template.queryForList(query, Article.class);
        articleList.stream().forEach(item -> System.out.println(item));
    }
}
