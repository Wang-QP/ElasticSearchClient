package cn.wang.es.repositories;

import cn.wang.es.entity.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ArticleRepository extends ElasticsearchRepository<Article,Long> {
}
