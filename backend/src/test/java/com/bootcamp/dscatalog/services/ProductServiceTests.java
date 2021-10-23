package com.bootcamp.dscatalog.services;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.bootcamp.dscatalog.dto.ProductDTO;
import com.bootcamp.dscatalog.entities.Category;
import com.bootcamp.dscatalog.entities.Product;
import com.bootcamp.dscatalog.repositories.CategoryRepository;
import com.bootcamp.dscatalog.repositories.ProductRepository;
import com.bootcamp.dscatalog.services.exceptions.DataBaseException;
import com.bootcamp.dscatalog.services.exceptions.ResourceNotFoundException;
import com.bootcamp.dscatalog.tests.Factory;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;
	@Mock
	private ProductRepository repository;
	@Mock
	private CategoryRepository categoryRepository;
	
	private Long existId;
	private Long notExistId;
	private Long dependenceId;
	private Product product;
	private Category category;
	private PageImpl<Product> page;
	
	@BeforeEach
	void setUp() throws Exception{
		existId = 1l;
		notExistId = 2000L;
		dependenceId = 4L;
		category = Factory.createdCategory();
		product = Factory.createdProduct();
		page = new PageImpl<>(Arrays.asList(product));
		
		when(repository.getOne(existId)).thenReturn(product);
		when(repository.getOne(notExistId)).thenThrow(ResourceNotFoundException.class);
		when(repository.findAll((Pageable)ArgumentMatchers.any())).thenReturn(page);
		when(categoryRepository.getOne(existId)).thenReturn(category);
		when(categoryRepository.getOne(notExistId)).thenThrow(ResourceNotFoundException.class);
		when(repository.save(ArgumentMatchers.any())).thenReturn(product);
		when(repository.findById(existId)).thenReturn(Optional.of(product));
		doNothing().when(repository).deleteById(existId);
		
		doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(notExistId);
		doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependenceId);
		doThrow(ResourceNotFoundException.class).when(repository).findById(notExistId);
	}
	
	@Test
	public void updateShouldUpdateObjectWhenIdExist() {
		ProductDTO dto = service.update(existId, Factory.createdProductDTO());
		
		Assertions.assertNotNull(dto);
		
		verify(repository, times(1)).getOne(existId);
		verify(repository, times(1)).save(product);
	}
	
	@Test
	public void updateShouldResourceNotFoundExceptionWhenNotExistedId() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(notExistId, Factory.createdProductDTO());
		});
		verify(repository, times(1)).getOne(notExistId);
	}
	
	@Test
	public void findByIdShouldReturnProductDTOWhenExistedId() {
		ProductDTO obj = service.findById(existId);
		Assertions.assertNotNull(obj); 
		
		verify(repository, times(1)).findById(existId);
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(notExistId);
		});
		verify(repository, times(1)).findById(notExistId);
	}
	
	@Test
	public void findAllPagedShouldReturnPage() {
		
		Pageable pageable = PageRequest.of(0, 10);
		Page<ProductDTO> pageDTO = service.findAllPaged(pageable);
		
		Assertions.assertNotNull(pageDTO);
		verify(repository, times(1)).findAll(pageable);
	}
	
	@Test
	public void deleteShouldThrowDataBaseExceptionWhenIdExistDependentCategory() {
		Assertions.assertThrows(DataBaseException.class,() -> {
			service.delete(dependenceId);
		});
		verify(repository, times(1)).deleteById(dependenceId);
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class,() -> {
			service.delete(notExistId);
		});
		verify(repository, times(1)).deleteById(notExistId);
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenIdExist() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existId);
		});
		verify(repository, times(1)).deleteById(existId);
	}
}
