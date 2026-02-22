package com.reliaquest.api.service;

import com.reliaquest.api.client.EmployeeApiClient;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.RateLimitException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeApiClient apiClient;

    public List<Employee> getAllEmployees() {
        log.debug("Fetching all employees");
        try {
            List<Employee> employees =
                    apiClient.execute("", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            log.debug("Found {} employees", employees != null ? employees.size() : 0);
            return employees != null ? employees : Collections.emptyList();
        } catch (RateLimitException e) {
            log.error("Rate limit hit while fetching employees");
            throw e;
        }
    }

    public List<Employee> searchEmployeesByName(String searchString) {
        log.debug("Searching for employees with name: {}", searchString);
        List<Employee> allEmployees = getAllEmployees();
        return allEmployees.stream()
                .filter(emp ->
                        emp.getName() != null && emp.getName().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Employee getEmployeeById(String id) {
        log.debug("Looking up employee with id: {}", id);
        try {
            Employee employee =
                    apiClient.execute("/" + id, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (employee == null) {
                log.warn("Employee with id {} not found", id);
                throw new EmployeeNotFoundException("Employee not found with ID: " + id);
            }
            return employee;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Employee with id {} not found", id);
            throw new EmployeeNotFoundException("Employee not found with ID: " + id);
        } catch (RateLimitException e) {
            log.error("Rate limit hit while getting employee {}", id);
            throw e;
        }
    }

    public Integer getHighestSalary() {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .map(Employee::getSalary)
                .filter(salary -> salary != null)
                .max(Integer::compareTo)
                .orElse(0);
    }

    public List<String> getTopTenHighestEarningEmployeeNames() {
        List<Employee> employees = getAllEmployees();
        return employees.stream()
                .filter(emp -> emp.getSalary() != null)
                .sorted(Comparator.comparing(Employee::getSalary).reversed())
                .limit(10)
                .map(Employee::getName)
                .collect(Collectors.toList());
    }

    public Employee createEmployee(EmployeeInput input) {
        log.info("Creating new employee: {}", input.getName());
        try {
            Employee employee = apiClient.execute("", HttpMethod.POST, input, new ParameterizedTypeReference<>() {});
            if (employee == null) {
                throw new RuntimeException("Failed to create employee");
            }
            log.info("Employee created successfully with id: {}", employee.getId());
            return employee;
        } catch (RateLimitException e) {
            log.error("Rate limit hit while creating employee");
            throw e;
        }
    }

    public String deleteEmployeeById(String id) {
        Employee employee = getEmployeeById(id);
        String name = employee.getName();

        log.info("Deleting employee: {} (id: {})", name, id);
        try {
            Map<String, String> deleteRequest = Map.of("name", name);
            apiClient.execute("", HttpMethod.DELETE, deleteRequest, new ParameterizedTypeReference<>() {});
            log.info("Employee {} deleted", name);
            return name;
        } catch (RateLimitException e) {
            log.error("Rate limit hit while deleting employee {}", name);
            throw e;
        }
    }
}
