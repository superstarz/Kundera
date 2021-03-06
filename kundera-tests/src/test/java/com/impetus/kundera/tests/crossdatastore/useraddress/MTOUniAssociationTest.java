/*******************************************************************************
 * * Copyright 2011 Impetus Infotech.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 ******************************************************************************/
package com.impetus.kundera.tests.crossdatastore.useraddress;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.IndexType;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.impetus.kundera.tests.cli.CassandraCli;
import com.impetus.kundera.tests.crossdatastore.useraddress.entities.HabitatUniMTo1;
import com.impetus.kundera.tests.crossdatastore.useraddress.entities.PersonnelUniMTo1;

/**
 * @author vivek.mishra
 * 
 */
public class MTOUniAssociationTest extends TwinAssociation
{
    /**
     * Inits the.
     */
    @BeforeClass
    public static void init() throws Exception
    {
        if (RUN_IN_EMBEDDED_MODE)
        {
            CassandraCli.cassandraSetUp();
        } else {
            if(AUTO_MANAGE_SCHEMA) {
                CassandraCli.initClient();
            }
        }
        
        List<Class> clazzz = new ArrayList<Class>(2);
        clazzz.add(PersonnelUniMTo1.class);
        clazzz.add(HabitatUniMTo1.class);
        init(clazzz, ALL_PUs_UNDER_TEST);
    }

    /**
     * Sets the up.
     * 
     * @throws Exception
     *             the exception
     */
    @Before
    public void setUp() throws Exception
    {
        setUpInternal();
    }

    /**
     * Test CRUD.
     */
    @Test
    public void testCRUD()
    {
        tryOperation();
    }

    @Override
    protected void insert()
    {
        PersonnelUniMTo1 person1 = new PersonnelUniMTo1();
        person1.setPersonId("unimanytoone_1");
        person1.setPersonName("Amresh");        

        PersonnelUniMTo1 person2 = new PersonnelUniMTo1();
        person2.setPersonId("unimanytoone_2");
        person2.setPersonName("Vivek");        

        HabitatUniMTo1 address = new HabitatUniMTo1();
        address.setAddressId("unimanytoone_a");
        address.setStreet("AAAAAAAAAAAAA");

        person1.setAddress(address);
        person2.setAddress(address);

        Set<PersonnelUniMTo1> persons = new HashSet<PersonnelUniMTo1>();
        persons.add(person1);
        persons.add(person2);

        dao.savePersons(persons);

        col.add(person1);
        col.add(person2);
        col.add(address);
    }

    @Override
    protected void find()
    {
        // Find Person 1
        PersonnelUniMTo1 p1 = (PersonnelUniMTo1) dao.findPerson(PersonnelUniMTo1.class, "unimanytoone_1");
        Assert.assertNotNull(p1);
        Assert.assertEquals("unimanytoone_1", p1.getPersonId());
        Assert.assertEquals("Amresh", p1.getPersonName());
        
        HabitatUniMTo1 add = p1.getAddress();
        Assert.assertNotNull(add);

        Assert.assertEquals("unimanytoone_a", add.getAddressId());
        Assert.assertEquals("AAAAAAAAAAAAA", add.getStreet());

        // Find Person 2
        PersonnelUniMTo1 p2 = (PersonnelUniMTo1) dao.findPerson(PersonnelUniMTo1.class, "unimanytoone_2");
        Assert.assertNotNull(p2);
        Assert.assertEquals("unimanytoone_2", p2.getPersonId());
        Assert.assertEquals("Vivek", p2.getPersonName());
        
        HabitatUniMTo1 add2 = p2.getAddress();
        Assert.assertNotNull(add2);

        Assert.assertEquals("unimanytoone_a", add2.getAddressId());
        Assert.assertEquals("AAAAAAAAAAAAA", add2.getStreet());

    }

    @Override
    protected void update()
    {
        // Find Person 1
        PersonnelUniMTo1 p1 = (PersonnelUniMTo1) dao.findPerson(PersonnelUniMTo1.class, "unimanytoone_1");
        Assert.assertNotNull(p1);
        p1.setPersonName("Saurabh");
        p1.getAddress().setStreet("Brand New Street");
        dao.merge(p1);
        PersonnelUniMTo1 p1AfterMerge = (PersonnelUniMTo1) dao.findPerson(PersonnelUniMTo1.class, "unimanytoone_1");
        Assert.assertNotNull(p1AfterMerge);
        Assert.assertEquals("Saurabh", p1AfterMerge.getPersonName());
        Assert.assertEquals("Brand New Street", p1AfterMerge.getAddress().getStreet());

        // Find Person 2
        PersonnelUniMTo1 p2 = (PersonnelUniMTo1) dao.findPerson(PersonnelUniMTo1.class, "unimanytoone_2");
        Assert.assertNotNull(p2);
        p2.setPersonName("Prateek");
        dao.merge(p2);
        PersonnelUniMTo1 p2AfterMerge = (PersonnelUniMTo1) dao.findPerson(PersonnelUniMTo1.class, "unimanytoone_2");
        Assert.assertNotNull(p2AfterMerge);
        Assert.assertEquals("Prateek", p2AfterMerge.getPersonName());
        Assert.assertEquals("Brand New Street", p2AfterMerge.getAddress().getStreet());
    }

    @Override
    protected void remove()
    {
        // Remove Person 1
        dao.remove("unimanytoone_1", PersonnelUniMTo1.class);
        PersonnelUniMTo1 p1AfterRemoval = (PersonnelUniMTo1) dao.findPerson(PersonnelUniMTo1.class, "unimanytoone_1");
        Assert.assertNull(p1AfterRemoval);

        // Remove Person 2
        dao.remove("unimanytoone_2", PersonnelUniMTo1.class);
        PersonnelUniMTo1 p2AfterRemoval = (PersonnelUniMTo1) dao.findPerson(PersonnelUniMTo1.class, "unimanytoone_2");
        Assert.assertNull(p2AfterRemoval);
    }

    /**
     * Tear down.
     * 
     * @throws Exception
     *             the exception
     */
    @After
    public void tearDown() throws Exception
    {
        tearDownInternal();        
    }

    @Override
    protected void loadDataForPERSONNEL() throws TException, InvalidRequestException, UnavailableException,
            TimedOutException, SchemaDisagreementException
    {
        KsDef ksDef = null;

        CfDef cfDef = new CfDef();
        cfDef.name = "PERSONNEL";
        cfDef.keyspace = "KunderaTests";
        //cfDef.column_type = "Super";
        cfDef.setComparator_type("UTF8Type");
        cfDef.setDefault_validation_class("UTF8Type");
        ColumnDef columnDef = new ColumnDef(ByteBuffer.wrap("PERSON_NAME".getBytes()), "UTF8Type");
        // columnDef.index_type = IndexType.KEYS;
        cfDef.addToColumn_metadata(columnDef);

        ColumnDef columnDef1 = new ColumnDef(ByteBuffer.wrap("ADDRESS_ID".getBytes()), "IntegerType");
        // columnDef1.index_type = IndexType.KEYS;
        cfDef.addToColumn_metadata(columnDef1);

        List<CfDef> cfDefs = new ArrayList<CfDef>();
        cfDefs.add(cfDef);

        try
        {
            ksDef = CassandraCli.client.describe_keyspace("KunderaTests");
            CassandraCli.client.set_keyspace("KunderaTests");

            List<CfDef> cfDefn = ksDef.getCf_defs();

            // CassandraCli.client.set_keyspace("KunderaTests");
            for (CfDef cfDef1 : cfDefn)
            {

                if (cfDef1.getName().equalsIgnoreCase("PERSONNEL"))
                {

                    CassandraCli.client.system_drop_column_family("PERSONNEL");

                }
            }
            CassandraCli.client.system_add_column_family(cfDef);

        }
        catch (NotFoundException e)
        {
            addKeyspace(ksDef, cfDefs);
        }

        CassandraCli.client.set_keyspace("KunderaTests");

    }

    @Override
    protected void loadDataForHABITAT() throws TException, InvalidRequestException, UnavailableException,
            TimedOutException, SchemaDisagreementException
    {
        KsDef ksDef = null;
        CfDef cfDef2 = new CfDef();
        cfDef2.name = "ADDRESS";
        cfDef2.keyspace = "KunderaTests";

        ColumnDef columnDef1 = new ColumnDef(ByteBuffer.wrap("STREET".getBytes()), "UTF8Type");
        columnDef1.index_type = IndexType.KEYS;
        cfDef2.addToColumn_metadata(columnDef1);

        List<CfDef> cfDefs = new ArrayList<CfDef>();
        cfDefs.add(cfDef2);

        try
        {
            ksDef = CassandraCli.client.describe_keyspace("KunderaTests");
            CassandraCli.client.set_keyspace("KunderaTests");
            List<CfDef> cfDefss = ksDef.getCf_defs();
            // CassandraCli.client.set_keyspace("KunderaTests");
            for (CfDef cfDef : cfDefss)
            {

                if (cfDef.getName().equalsIgnoreCase("ADDRESS"))
                {

                    CassandraCli.client.system_drop_column_family("ADDRESS");

                }
            }
            CassandraCli.client.system_add_column_family(cfDef2);
        }
        catch (NotFoundException e)
        {
            addKeyspace(ksDef, cfDefs);
        }
        CassandraCli.client.set_keyspace("KunderaTests");

    }

}
