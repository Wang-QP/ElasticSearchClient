package cn.wang.es.repositories;

import cn.wang.es.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ArticleRepository extends ElasticsearchRepository<Article,Long> {
    // 根据命名规则查询
    List<Article> findByTitle(String title);

    List<Article> findAllByTitleOrContent(String title, String content);
    List<Article> findAllByTitleOrContent(String title, String content, Pageable pageable);
}
