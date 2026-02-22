package com.reliaquest.api.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.EmployeeService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id("1")
                .name("John Doe")
                .salary(100000)
                .age(30)
                .title("Engineer")
                .build();
    }

    @Test
    void getAllEmployees_shouldReturnEmployeeList() {
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(employee));

        ResponseEntity<List<Employee>> response = employeeController.getAllEmployees();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0).getName());
    }

    @Test
    void getEmployeesByNameSearch_shouldReturnFilteredList() {
        when(employeeService.searchEmployeesByName(anyString())).thenReturn(Arrays.asList(employee));

        ResponseEntity<List<Employee>> response = employeeController.getEmployeesByNameSearch("John");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        when(employeeService.getEmployeeById(anyString())).thenReturn(employee);

        ResponseEntity<Employee> response = employeeController.getEmployeeById("1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    void getHighestSalary_shouldReturnInteger() {
        when(employeeService.getHighestSalary()).thenReturn(150000);

        ResponseEntity<Integer> response = employeeController.getHighestSalaryOfEmployees();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(150000, response.getBody());
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnNames() {
        List<String> names = Arrays.asList("John Doe", "Jane Smith");
        when(employeeService.getTopTenHighestEarningEmployeeNames()).thenReturn(names);

        ResponseEntity<List<String>> response = employeeController.getTopTenHighestEarningEmployeeNames();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("John Doe", response.getBody().get(0));
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
        EmployeeInput input = EmployeeInput.builder()
                .name("John Doe")
                .salary(100000)
                .age(30)
                .title("Engineer")
                .build();

        when(employeeService.createEmployee(any(EmployeeInput.class))).thenReturn(employee);

        ResponseEntity<Employee> response = employeeController.createEmployee(input);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John Doe", response.getBody().getName());
    }

    @Test
    void deleteEmployeeById_shouldReturnEmployeeName() {
        when(employeeService.deleteEmployeeById(anyString())).thenReturn("John Doe");

        ResponseEntity<String> response = employeeController.deleteEmployeeById("1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("John Doe", response.getBody());
    }
}
