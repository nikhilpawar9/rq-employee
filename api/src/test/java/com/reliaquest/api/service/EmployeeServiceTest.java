package com.reliaquest.api.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeApiClient apiClient;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    void setUp() {
        employee1 = Employee.builder()
                .id("1")
                .name("John Doe")
                .salary(100000)
                .age(30)
                .title("Engineer")
                .build();

        employee2 = Employee.builder()
                .id("2")
                .name("Jane Smith")
                .salary(120000)
                .age(35)
                .title("Senior Engineer")
                .build();
    }

    @Test
    void getAllEmployees_shouldReturnEmployeeList() {
        when(apiClient.execute(eq(""), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(Arrays.asList(employee1, employee2));

        List<Employee> result = employeeService.getAllEmployees();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void searchEmployeesByName_shouldFilterByName() {
        when(apiClient.execute(eq(""), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(Arrays.asList(employee1, employee2));

        List<Employee> result = employeeService.searchEmployeesByName("John");

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
    }

    @Test
    void getEmployeeById_shouldReturnEmployee() {
        when(apiClient.execute(eq("/1"), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(employee1);

        Employee result = employeeService.getEmployeeById("1");

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void getEmployeeById_shouldThrowExceptionWhenNotFound() {
        when(apiClient.execute(anyString(), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(null);

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById("999"));
    }

    @Test
    void getHighestSalary_shouldReturnMaxSalary() {
        when(apiClient.execute(eq(""), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(Arrays.asList(employee1, employee2));

        Integer result = employeeService.getHighestSalary();

        assertEquals(120000, result);
    }

    @Test
    void getTopTenHighestEarningEmployeeNames_shouldReturnSortedNames() {
        when(apiClient.execute(eq(""), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(Arrays.asList(employee1, employee2));

        List<String> result = employeeService.getTopTenHighestEarningEmployeeNames();

        assertEquals(2, result.size());
        assertEquals("Jane Smith", result.get(0));
        assertEquals("John Doe", result.get(1));
    }

    @Test
    void createEmployee_shouldReturnCreatedEmployee() {
        EmployeeInput input = EmployeeInput.builder()
                .name("New Employee")
                .salary(90000)
                .age(28)
                .title("Developer")
                .build();

        when(apiClient.execute(eq(""), eq(HttpMethod.POST), eq(input), any(ParameterizedTypeReference.class)))
                .thenReturn(employee1);

        Employee result = employeeService.createEmployee(input);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    void deleteEmployeeById_shouldReturnEmployeeName() {
        when(apiClient.execute(eq("/1"), eq(HttpMethod.GET), isNull(), any(ParameterizedTypeReference.class)))
                .thenReturn(employee1);
        when(apiClient.execute(eq(""), eq(HttpMethod.DELETE), anyMap(), any(ParameterizedTypeReference.class)))
                .thenReturn(true);

        String result = employeeService.deleteEmployeeById("1");

        assertEquals("John Doe", result);
    }
}
