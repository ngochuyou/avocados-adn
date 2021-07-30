/**
 * 
 */
package adn.controller;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;

import adn.dao.DatabaseInteractionResult;
import adn.helpers.ArrayHelper;
import adn.model.entities.Product;
import adn.service.internal.ResourceService;
import adn.service.services.DepartmentService;
import adn.service.services.ProductService;

/**
 * @author Ngoc Huy
 *
 */
@Controller
@RequestMapping("/product")
public class ProductController extends DepartmentScopedController {

	protected final ProductService productService;

	protected final ResourceService resourceService;

	@Autowired
	public ProductController(DepartmentService departmentService, ProductService productService,
			ResourceService resourceService) {
		super(departmentService);
		this.productService = productService;
		this.resourceService = resourceService;
	}

	@GetMapping(path = "/image/{filename:.+}")
	public Object obtainProductImage(@PathVariable(name = "filename", required = true) String filename)
			throws NoSuchFieldException {
		try {
			return makeStaleWhileRevalidate(resourceService.directlyGetProductImageBytes(filename), 180, TimeUnit.DAYS,
					365, TimeUnit.DAYS);
		} catch (IOException e) {
			return fails(String.format("Unable to get image %s", filename));
		}
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public @ResponseBody ResponseEntity<?> createProduct(@RequestPart(name = "model", required = true) String jsonPart,
			@RequestPart(name = "images", required = false) MultipartFile[] images) {
		assertSaleDepartment();

		Product model;

		try {
			model = objectMapper.readValue(jsonPart, Product.class);
		} catch (JsonProcessingException any) {
			any.printStackTrace();
			return ResponseEntity.badRequest().body(INVALID_MODEL);
		}

		DatabaseInteractionResult<Product> result = productService.createProduct(model, images, true);

		return send(result);
	}

	@PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Secured("ROLE_PERSONNEL")
	@Transactional
	public @ResponseBody ResponseEntity<?> updateProduct(@RequestPart(name = "model", required = true) Product model,
			@RequestPart(name = "images", required = false) MultipartFile[] images) {
		assertSaleDepartment();

//		Product model;
//
//		try {
//			model = objectMapper.readValue(jsonPart, Product.class);
//		} catch (JsonProcessingException jpe) {
//			return sendBadRequest("Invalid model");
//		}

		Product persistence = baseRepository.findById(model.getId(), Product.class);

		if (persistence == null) {
			return sendNotFound(String.format("Product %s not found", model.getId()));
		}

		DatabaseInteractionResult<Product> result = productService.updateProduct(model,
				ArrayHelper.from(images, MultipartFile.class), true);

		return send(result);
	}

}
