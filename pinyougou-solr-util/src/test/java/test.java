import org.apache.solr.client.solrj.response.UpdateResponse;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;

public class test {
    @Test
    public void delete() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
        SolrTemplate solrTemplate = ac.getBean(SolrTemplate.class);
        Query query = new SimpleQuery("*:*");
        UpdateResponse delete = solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
