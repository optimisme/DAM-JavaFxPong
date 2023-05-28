import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "Employee", 
   uniqueConstraints = {@UniqueConstraint(columnNames = "id")})
public class Employee implements Serializable {
    
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(name = "id", unique = true, nullable = false)
      private long employeeId;
   
      @Column(name = "firstName")
      private String firstName; 
      
      @Column(name = "lastName")
      private String lastName;   
      
      @Column(name = "salary")
      private int salary;  

      @ManyToMany(mappedBy = "employees")   
      private Set<Contact> contacts; // No té getter i setter perqué s'encarrega Contact.employees"

      public Employee() {}
    
      public Employee(String fname, String lname, int salary) {
         this.firstName = fname;
         this.lastName = lname;
         this.salary = salary;
      }

      public long getEmployeeId() {
         return employeeId;
      }
    
      public void setEmployeeId(long id) {
         this.employeeId = id;
      }
    
      public String getFirstName() {
         return firstName;
      }
    
      public void setFirstName(String first_name) {
         this.firstName = first_name;
      }
    
      public String getLastName() {
         return lastName;
      }
    
      public void setLastName(String last_name) {
         this.lastName = last_name;
      }
    
      public int getSalary() {
         return salary;
      }
    
      public void setSalary(int salary) {
         this.salary = salary;
      }

      @Override
      public String toString () {
         return this.getEmployeeId() + ": " + this.getFirstName() + " " + this.getLastName() + ", " + this.getSalary();
      }
 }