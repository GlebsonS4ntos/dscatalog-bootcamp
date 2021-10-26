package com.bootcamp.dscatalog.services.validation;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.bootcamp.dscatalog.dto.UserInsertDTO;
import com.bootcamp.dscatalog.entities.User;
import com.bootcamp.dscatalog.repositories.UserRepository;
import com.bootcamp.dscatalog.resources.exception.FieldMessage;

public class UserInsertValidator implements ConstraintValidator<UserInsertValid, UserInsertDTO> {
	
	@Autowired
	private UserRepository repository;
	
	@Override
	public void initialize(UserInsertValid ann) {
	}

	@Override
	public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {
		
		User user = repository.findByEmail(dto.getEmail());
		if(user != null ){
			FieldMessage e = new FieldMessage("email", "Email Existente");
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getField())
					.addConstraintViolation();
			return false;

		}
		return true;
	}
}
