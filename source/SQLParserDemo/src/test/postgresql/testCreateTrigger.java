package test.postgresql;
/*
 * Date: 13-12-5
 */

import gudusoft.gsqlparser.EDataType;
import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.nodes.TColumnDefinition;
import gudusoft.gsqlparser.stmt.TCreateTableSqlStatement;
import gudusoft.gsqlparser.stmt.postgresql.TPlsqlCreateTriggerSqlStatement;
import junit.framework.TestCase;

public class testCreateTrigger extends TestCase {
    public void test1(){

        TGSqlParser sqlparser = new TGSqlParser(EDbVendor.dbvpostgresql);
        sqlparser.sqltext = "CREATE TRIGGER emp_stamp BEFORE INSERT OR UPDATE ON EMP\n" +
                "FOR EACH ROW EXECUTE PROCEDURE emp_stamp();";
        assertTrue(sqlparser.parse() == 0);

        TPlsqlCreateTriggerSqlStatement creatTrigger  = (TPlsqlCreateTriggerSqlStatement)sqlparser.sqlstatements.get(0);
        assertTrue(creatTrigger.getTriggerName().toString().equalsIgnoreCase("emp_stamp"));
        assertTrue(creatTrigger.getOnTable().getTableName().toString().equalsIgnoreCase("EMP"));
        //System.out.println(creatTrigger.getFunctionCall().getFunctionName().toString());
        assertTrue(creatTrigger.getFunctionCall().getFunctionName().toString().equalsIgnoreCase("emp_stamp"));
        assertTrue(creatTrigger.getFunctionCall().getArgs() == null);

    }

}
