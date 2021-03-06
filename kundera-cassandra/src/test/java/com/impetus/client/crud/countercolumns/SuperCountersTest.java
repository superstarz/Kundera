/**
 * 
 */
package com.impetus.client.crud.countercolumns;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.thrift.Cassandra.Client;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.impetus.client.persistence.CassandraCli;

/**
 * @author impadmin
 * 
 */
public class SuperCountersTest
{
    private EntityManagerFactory emf;

    private EntityManager em;

    private static final boolean RUN_IN_EMBEDDED_MODE = true;

    private static final boolean AUTO_MANAGE_SCHEMA = true;

    private String keyspace = "KunderaCounterColumn";

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        if (RUN_IN_EMBEDDED_MODE)
        {
            startServer();
        }

        if (AUTO_MANAGE_SCHEMA)
        {
            createSchema();
        }
        emf = Persistence.createEntityManagerFactory("CassandraCounterTest");
    }

    private void createSchema() throws InvalidRequestException, TException, SchemaDisagreementException
    {
        Client client = CassandraCli.getClient();
        CassandraCli.createKeySpace(keyspace);
        client.set_keyspace(keyspace);

        CfDef cfDef = new CfDef();
        cfDef.keyspace = keyspace;
        cfDef.name = "SuperCounters";
        cfDef.column_type = "Super";
        cfDef.default_validation_class = "CounterColumnType";
        cfDef.comparator_type = "UTF8Type";
        client.system_add_column_family(cfDef);
    }

    private void startServer() throws IOException, TException, InvalidRequestException, UnavailableException,
            TimedOutException, SchemaDisagreementException
    {
        CassandraCli.cassandraSetUp();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        if (AUTO_MANAGE_SCHEMA && CassandraCli.keyspaceExist(keyspace))
        {
            CassandraCli.client.system_drop_keyspace(keyspace);
        }
        emf.close();
    }

    @Test
    public void testCRUDOnCounter()
    {
        persistSuperCounter();
        findSuperCounter();
        deleteSuperCounter();
    }

    private void deleteSuperCounter()
    {
        em = emf.createEntityManager();
        SuperCounters counters = new SuperCounters();
        counters = em.find(SuperCounters.class, "sk");
        Assert.assertNotNull(counters);
        Assert.assertNotNull(counters.getCounter());
        Assert.assertNotNull(counters.getSubCounter());
        Assert.assertNotNull(counters.getSubCounter().getSubCounter());
        em.remove(counters);

        EntityManager em1 = emf.createEntityManager();
        counters = em1.find(SuperCounters.class, "sk");
        Assert.assertNull(counters);

        em.close();

    }

    private void findSuperCounter()
    {

        em = emf.createEntityManager();
        SuperCounters superCounters = new SuperCounters();
        superCounters = em.find(SuperCounters.class, "sk");
        Assert.assertNotNull(superCounters);
        Assert.assertNotNull(superCounters.getCounter());
        em.close();
    }

    private void persistSuperCounter()
    {

        em = emf.createEntityManager();
        SuperCounters superCounters = new SuperCounters();
        superCounters.setCounter(12);
        superCounters.setId("sk");

        SubCounter subCounter = new SubCounter();
        subCounter.setSubCounter(23);

        superCounters.setSubCounter(subCounter);

        em.persist(superCounters);

        EntityManager newEM = emf.createEntityManager();
        superCounters = newEM.find(SuperCounters.class, "sk");
        Assert.assertNotNull(superCounters);
        Assert.assertNotNull(superCounters.getCounter());

        em.close();
        newEM.close();
    }
}
