package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;


public class App
{
    public static void main(String[] args)
    {
        // Create new Application
        App a = new App();

        // Connect to database
        a.connect();

        //Get employee
        //Employee emp = a.getEmployee(255530);

        // Display employee
        //a.displayEmployee(emp);

        // Get all employees' salary information
        //ArrayList<Employee> employees = a.getAllSalaries();

        // Test the size of the returned data - should be 240124
        //System.out.println("Number of employees: " + employees.size());

        //Display all employees' salary information
        //a.printSalaries(employees);

        //Get and display employees' salary by a role (eg: Engineer)
        ArrayList<Employee> salaryByRoleList = a.getSalariesByRole("Engineer");
        a.printSalaries(salaryByRoleList);

        // Disconnect from database
        a.disconnect();
    }

    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect()
    {
        try
        {
            // Load Database driver
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i)
        {
            System.out.println("Connecting to database...");
            try
            {
                // Wait a bit for db to start
                Thread.sleep(30000);
                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://db:3306/employees?useSSL=false", "root", "example");
                System.out.println("Successfully connected");
                break;
            }
            catch (SQLException sqle)
            {
                System.out.println("Failed to connect to database attempt " + Integer.toString(i));
                System.out.println(sqle.getMessage());
            }
            catch (InterruptedException ie)
            {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect()
    {
        if (con != null)
        {
            try
            {
                // Close connection
                con.close();
            }
            catch (Exception e)
            {
                System.out.println("Error closing connection to database");
            }
        }
    }

    public Employee getEmployee(int ID)
    {

        //MY NEW MYSQL ADDITION DOESN'T WORK... WHY?, HELP?

        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =

            "SELECT employees.emp_no, first_name, last_name, title, salary, departments.dept_name, dept_manager.dept_no, dept_manager.emp_no "
                + "FROM employees JOIN titles ON (employees.emp_no=titles.emp_no) "
                + "JOIN salaries ON (employees.emp_no=salaries.emp_no) "
                + "JOIN dept_emp ON (employees.emp_no=dept_emp.emp_no) "
                + "JOIN departments ON (dept_emp.dept_no=departments.dept_no) "
                + "JOIN dept_manager ON (departments.dept_no=dept_manager.dept_no) "
                + "WHERE employees.emp_no = " + ID
                + " AND salaries.to_date = '9999-01-01'"
                + " AND dept_manager.to_date = '9999-01-01'";

            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Return new employee if valid.
            // Check one is returned
            if (rset.next())
            {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.title = rset.getString("title");
                emp.salary = rset.getInt("salary");
                emp.dept_name = rset.getString("departments.dept_name");
                emp.manager = rset.getString("dept_manager.emp_no") + " " +rset.getString("dept_manager.dept_no");
                return emp;
            }
            else
                return null;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    public void displayEmployee(Employee emp)
    {
        if (emp != null)
        {
            System.out.println(
                    emp.emp_no + " "
                            + emp.first_name + " "
                            + emp.last_name + "\n"
                            + emp.title + "\n"
                            + "Salary:" + emp.salary + "\n"
                            + emp.dept_name + "\n"
                            + "Manager: " + emp.manager + "\n");
        }
    }

    /**
     * Gets all the current employees and salaries.
     * @return A list of all employees and salaries, or null if there is an error.
     */
    public ArrayList<Employee> getAllSalaries()
    {
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
                            + "FROM employees, salaries "
                            + "WHERE employees.emp_no = salaries.emp_no AND salaries.to_date = '9999-01-01' "
                            + "ORDER BY employees.emp_no ASC";
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Extract employee information
            ArrayList<Employee> employees = new ArrayList<Employee>();
            while (rset.next())
            {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");
                employees.add(emp);
            }
            return employees;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary details");
            return null;
        }
    }

    /**
     * Prints a list of employees.
     * @param employees The list of employees to print.
     */
    public void printSalaries(ArrayList<Employee> employees)
    {
        // Print header
        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));
        // Loop over all employees in the list
        for (Employee emp : employees)
        {
            String emp_string =
                    String.format("%-10s %-15s %-20s %-8s",
                            emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }
    }

    /**
     * Get salaries by a role
     * @param role The string of the role that the salary information is wanted for
     * @return ArrayList of employees and their salary information
     */
    public ArrayList<Employee> getSalariesByRole(String role)
    {

        try
        {
            Statement stmt = con.createStatement();

            ArrayList<Employee> salaryList = new ArrayList<Employee>();

            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
                    + "FROM employees JOIN salaries ON (employees.emp_no=salaries.emp_no) "
                    + "JOIN titles ON (employees.emp_no=titles.emp_no) "
                    + "WHERE salaries.to_date = '9999-01-01' "
                    + "AND titles.to_date = '9999-01-01' "
                    + "AND titles.title = " + role
                    + " ORDER BY employees.emp_no ASC" ;

            ResultSet rset = stmt.executeQuery(strSelect);

            while(rset.next())
            {
                Employee empl = new Employee();
                empl.emp_no = rset.getInt("employees.emp_no");
                empl.first_name = rset.getString("employees.first_name");
                empl.last_name = rset.getString("employees.last_name");
                empl.salary = rset.getInt("salaries.salary");
                salaryList.add(empl);
            }

            return salaryList;

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to print the salaries by role...");
            return null;
        }

    }

}

