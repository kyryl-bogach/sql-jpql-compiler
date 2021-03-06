package transformer.client;

import transformer.factory.JPQLTransformerFactory;
import transformer.factory.JPQLTransformers;
import transformer.service.JPQLTransformer;
import validator.service.Validator;

public class Cliente {

	public static void main(String[] args) throws Exception {

		final boolean validate = true;
		final boolean saveResults = true;
		
		JPQLTransformer transfomer = JPQLTransformerFactory.getInstance(JPQLTransformers.GSP);

		String sql = "SELECT * FROM Customers WHERE (customerId = 2 and (customerId = 2 or customername = 'pedro')) or (Customers.customerId = 3)";
		
		sql = "SELECT * FROM Customers";
		//sql = "SELECT * FROM Customers c WHERE (c.customerid = 1 and (1 = 1 or 1 = 0)) and (c.customerid = 1)";	
		//sql = "SELECT * FROM Customers, Orders";
		//sql = "SELECT * FROM Customers";
		//sql = "SELECT c.* FROM Customers c, Orders o"; 
		//sql = "SELECT customers.* FROM Customers";
		//sql = "SELECT ord.OrderID, Customers.CustomerName FROM Orders ord, Customers";
		//sql = "SELECT OrderID FROM Orders";
		//sql = "SELECT CUSTOMERNAME FROM CUSTOMERS, ORDERS";
		//sql = "SELECT CUSTOMERNAME as campoguay FROM CUSTOMERS, ORDERS";
		//sql = "SELECT CUSTOMERNAME as campoguay FROM CUSTOMERS, ORDERS WHERE customername = 'Pepe'";
		

		//sql = "SELECT CUSTOMERNAME as campoguay FROM CUSTOMERS, ORDERS WHERE customers.customername = 'Pepe'";
		//sql = "SELECT CUSTOMERNAME as campoguay FROM CUSTOMERS custom, ORDERS WHERE custom.customername = 'Pepe'";
		

		
		//sql = "SELECT Orders.OrderID, Customers.CustomerName, Orders.OrderDate FROM Orders INNER JOIN Customers ON Orders.CustomerID=Customers.CustomerID";
		sql = "SELECT o.OrderID, c.CustomerName, o.OrderDate FROM Orders o INNER JOIN Customers c ON o.CustomerID=c.CustomerID";
		sql = "SELECT * FROM PRODUCTS JOIN CATEGORIES ON PRODUCTS.CATEGORYID=CATEGORIES.CATEGORYID";
		sql = "SELECT * FROM Products p JOIN Categories c ON p.CATEGORYID=c.CATEGORYID";

		sql = "SELECT p.*, c.categoryid FROM Products p LEFT JOIN Categories c ON DESCRIPTION = 'test'";
		sql = "SELECT p.*, c.categoryid FROM Products p JOIN Categories c ON 1=1";
		
		
		sql = "SELECT CustomerID, Country " 
				+ "FROM Customers " 
				+ "GROUP BY Country "
				+ "HAVING CustomerID > 5 "
				+ "ORDER BY CustomerID DESC";
		
		sql = "SELECT p.*, c.categoryid FROM Products p JOIN Categories c ON (categoryName > 'pf') OR   p.CATEGORYID=c.CATEGORYID";
		sql = "SELECT * FROM employees " 
				+ "LEFT JOIN orders ON orders.EmployeeID = employees.EmployeeID";
		sql = "SELECT * FROM orders o " 
				+ "LEFT JOIN employees e ON o.EmployeeID = e.EmployeeID";
		
		sql= "SELECT * FROM Customers c, Orders o WHERE (c.cuStoMerid = 2 and (orders.OrderID = 2 or o.OrderID = 'pedro')) or (Customers.cuStomerId = 3) or (orderID = '2') or (orderID IN (1,'ab'))";
		
		sql = "SELECT p.*, c.categoryid FROM Products p JOIN Categories c ON (categoryName > 'pf') OR   p.CATEGORYID=c.CATEGORYID";
		
		sql = "SELECT p.categoryid, c.categoryid FROM Products p JOIN Categories c ON p.CATEGORYID=c.CATEGORYID";
		sql = "SELECT COUNT(CustomerID), Country " 
				+ "FROM Customers " 
				+ "GROUP BY Country "
				+ "HAVING COUNT(CustomerID) > 2 ";
		
		
		
		//sql = "SELECT productId FROM Order_Details WHERE Quantity = 10";
		
		//sql = "SELECT * FROM Customers c, Orders o WHERE (c.cuStoMerid = 2 and (orders.OrderID = 2 or o.OrderID = 'pedro')) or (Customers.cuStomerId = 3) or (orderID = '2') or (orderID IN (1,'ab'))";
		
		/*sql = "SELECT COUNT(CustomerID ,2), Country " 
				+ "FROM Customers " 
				+ "GROUP BY Country "
				+ "HAVING COUNT(CustomerID) > 2 ";*/
		
		
		// Estas dos con RIGHT no funcionan, pero s� LEFT (ni idea de por qu�)
		/*sql = "SELECT Orders.OrderID, Employees.LastName, Employees.FirstName "
			+ "FROM Orders "
			+ "RIGHT OUTER JOIN Employees ON Orders.EmployeeID = Employees.EmployeeID "
			+ "ORDER BY Orders.OrderID";
		
		sql = "SELECT Orders.OrderID, Employees.LastName, Employees.FirstName "
				+ "FROM Orders "
				+ "RIGHT JOIN Employees ON Orders.EmployeeID = Employees.EmployeeID "
				+ "ORDER BY Orders.OrderID";*/
		
		// FULL OUTER JOIN no existe en MySQL
		/*sql = "SELECT Customers.CustomerName, Orders.OrderID "
				+ "FROM Customers "
				+ "FULL OUTER JOIN Orders ON Customers.CustomerID=Orders.CustomerID "
				+ "ORDER BY Customers.CustomerName";*/
		
		//sql = "SELECT Customers.CustomerName, Orders.OrderID FROM Customers JOIN Orders ON Customers.CustomerID=Orders.CustomerID ORDER BY Customers.CustomerName";

		//sql = "SELECT * FROM Customers_extra";
		//sql = "SELECT last_message, mobile_phone FROM Customers_extra";
		//sql = "SELECT * FROM customers JOIN customers_extra ON 1=1";
		
		// ONE TO ONE
		//sql = "SELECT * FROM customers JOIN customers_extra ON customers.CustomerID = customers_extra.customer_ID";
		//sql = "SELECT * FROM customers JOIN customers_extra ON customers_extra.customer_ID = customers.CustomerID";
		//sql = "SELECT * FROM customers c JOIN customers_extra e ON e.customer_ID = c.CustomerID";
		
		//sql = "SELECT * FROM customers_extra JOIN customers ON customers.CustomerID = customers_extra.customer_ID";
		//sql = "SELECT * FROM customers_extra JOIN customers ON customers_extra.customer_ID = customers.CustomerID";

		//sql = "SELECT * FROM Products p JOIN Categories c ON p.CATEGORYID=c.CATEGORYID";
		
		sql = "SELECT COUNT(*), Country " 
				+ "FROM Customers " 
				+ "GROUP BY Country "
				+ "HAVING COUNT(CustomerID) > 2 ";
		
		//sql = "SELECT ProductName FROM Products p WHERE p.producTiD = ANY (SELECT productId FROM Products WHERE price > 10)";
		//sql = "SELECT ProductName FROM Products p WHERE p.ProductID = 2 OR 1 = 'ProductID' AND p.producTiD = ANY (SELECT productId FROM Products WHERE price > 10)";
		
		//sql = "SELECT ProductName FROM Products WHERE ProductID = ANY (SELECT ProductID FROM Order_Details WHERE Quantity = 10)";
		
		//sql = "SELECT productId FROM Order_Details WHERE Quantity > 10";
		//sql = "SELECT o.productId FROM Order_Details o WHERE Quantity > 10";
		
		
		//sql = "SELECT * FROM employees LEFT JOIN orders ON orders.EmployeeID = employees.EmployeeID";
		//sql = "SELECT p.*, c.categoryid FROM Products p JOIN Categories c ON 1=1";
		sql = "SELECT replacement_cost AS ReplacementCost, replacement_cost / 5 AS DecimalRentalRate, replacement_cost DIV 5 AS IntegerRentalRate, replacement_cost % 5 AS RemainderRentalRate FROM film";
		sql = "SELECT rental_rate AS RentalRate, rental_rate + 3 * 4 - 1 AS Result1, rental_rate * 4 - 1 AS Result2, rental_rate + 3 * 4 -1 AS Result3, rental_rate * 4 AS Result4 FROM film";
		
		sql = "SELECT actor.first_name, actor.last_name FROM film_actor INNER JOIN actor ON film_actor.actor_id = actor.actor_id GROUP BY actor.first_name, actor.last_name";
		sql = "SELECT actor_id, MAX(film_id) FROM film_actor GROUP BY actor_id";

		//sql = "SELECT film.film_id, film.title, film.release_year, actor.actor_id, actor.first_name, actor.last_name FROM film INNER JOIN film_actor ON film_actor.film_id = film.film_id INNER JOIN actor ON actor.actor_id = film.actor_id";
		sql = "SELECT * FROM actor INNER JOIN film ON actor.film_id = film.film_id WHERE film.title = 'Academy Dinosaur'";
		sql = "SELECT * FROM address ORDER BY district, postal_code DESC";
		
		
		sql = "SELECT address, city from address JOIN city ON address.city_id = city.city_id";
		
		
		sql = "SELECT film.film_id, film.title, film.release_year, actor.actor_id, actor.first_name, actor.last_name FROM film INNER JOIN film_actor ON film.film_id = film_actor.film_id INNER JOIN actor ON actor.actor_id = film_actor.actor_id";

		sql = "SELECT actor.first_name, actor.last_name, COUNT(*) FROM film_actor INNER JOIN actor ON film_actor.actor_id = actor.actor_id GROUP BY actor.first_name, actor.last_name";
		sql = "SELECT actor.first_name, actor.last_name, COUNT(*) FROM film_actor INNER JOIN actor ON film_actor.actor_id = actor.actor_id GROUP BY film_actor.actor_id";

		sql = "SELECT actor.first_name, actor.last_name, film_actor.* FROM actor INNER JOIN film_actor ON actor.actor_id = film_actor.actor_id ORDER BY actor_id";

		sql = "SELECT film.film_id, film_actor.actor_id FROM film INNER JOIN film_actor ON film_actor.film_id = film.film_id WHERE film.film_id = 1";
		
		sql = "SELECT * FROM film, film_actor WHERE film_actor.film_id = film.film_id";
		sql = "SELECT film.film_id, film.title, film.release_year, actor.actor_id, actor.first_name, actor.last_name FROM film INNER JOIN film_actor ON film.film_id = film_actor.film_id INNER JOIN actor ON actor.actor_id = film_actor.actor_id;";
		

		sql = "SELECT COUNT(*), Country " 
				+ "FROM Customers " 
				+ "GROUP BY Country "
				+ "HAVING COUNT(CustomerID) > 2 ";
		System.out.println("SQL  => " + sql + "\n");
		
		String jpql = transfomer.transform(sql);
		
		System.out.println("\nJPQL => " + jpql);
		
		if (validate) {
			String resultado = Validator.validate(sql, jpql, saveResults).getMensaje();
			System.out.println(resultado);
		}
	}

}
