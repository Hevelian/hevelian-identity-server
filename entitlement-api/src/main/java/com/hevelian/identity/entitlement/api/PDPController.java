package com.hevelian.identity.entitlement.api;

import com.hevelian.identity.core.api.PrimitiveResult;
import com.hevelian.identity.core.api.pagination.PageRequestParameters;
import com.hevelian.identity.core.api.pagination.PageRequestParametersReader;
import com.hevelian.identity.core.pagination.PageRequestBuilder;
import com.hevelian.identity.core.specification.EntitySpecificationsBuilder;
import com.hevelian.identity.entitlement.PDPService;
import com.hevelian.identity.entitlement.PDPService.PDPPolicyCombiningAlgorithmNotSupportedException;
import com.hevelian.identity.entitlement.PDPService.PDPPolicyNotFoundByPolicyIdException;
import com.hevelian.identity.entitlement.api.dto.EnableDisablePolicyRequestDTO;
import com.hevelian.identity.entitlement.api.dto.OrderPolicyRequestDTO;
import com.hevelian.identity.entitlement.api.dto.PolicyIdDTO;
import com.hevelian.identity.entitlement.model.PolicyType;
import com.hevelian.identity.entitlement.model.pdp.PDPConfig;
import com.hevelian.identity.entitlement.model.pdp.PDPPolicy;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.wso2.balana.combine.xacml2.FirstApplicablePolicyAlg;
import org.wso2.balana.combine.xacml2.OnlyOneApplicablePolicyAlg;
import org.wso2.balana.combine.xacml3.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/PDPService")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Validated
public class PDPController {
  private final PDPService pdpService;

  @RequestMapping(path = "/deletePolicy", method = RequestMethod.POST)
  public void deletePolicy(@Valid @RequestBody PolicyIdDTO pdpPolicyIdDTO)
      throws PDPPolicyNotFoundByPolicyIdException {
    pdpService.deletePolicy(pdpPolicyIdDTO.getPolicyId());
  }

  @RequestMapping(path = "/enableDisablePolicy", method = RequestMethod.POST)
  public void enableDisablePolicy(
      @Valid @RequestBody EnableDisablePolicyRequestDTO enableDisablePolicyRequestDTO)
      throws PDPPolicyNotFoundByPolicyIdException {
    pdpService.enableDisablePolicy(enableDisablePolicyRequestDTO.getPolicyId(),
        enableDisablePolicyRequestDTO.getEnable());
  }

  @RequestMapping(path = "/orderPolicy", method = RequestMethod.POST)
  public void orderPolicy(@Valid @RequestBody OrderPolicyRequestDTO orderPolicyRequestDTO)
      throws PDPPolicyNotFoundByPolicyIdException {
    pdpService.orderPolicy(orderPolicyRequestDTO.getPolicyId(), orderPolicyRequestDTO.getOrder());
  }

  @RequestMapping(path = "/getPolicy", method = RequestMethod.POST)
  public PDPPolicy getPolicy(@Valid @RequestBody PolicyIdDTO policyIdDTO)
      throws PDPPolicyNotFoundByPolicyIdException {
    return pdpService.getPolicy(policyIdDTO.getPolicyId());
  }

  // TODO Maybe this method should not return content. It can be returned by
  // getPolicy or getPolicyContent
  @RequestMapping(path = "/getAllPolicies", method = RequestMethod.GET)
  public Page<PDPPolicy> getAllPolicies(@ApiParam(value = PageRequestParameters.PAGE_DESCRIPTION,defaultValue = PageRequestParameters.DEFAULT_PAGE) @RequestParam(name = PageRequestParameters.PAGE, required = false) @Min(PageRequestParameters.PAGE_MIN) Integer page,
                                        @ApiParam(value = PageRequestParameters.SIZE_DESCRIPTION,defaultValue = PageRequestParameters.DEFAULT_SIZE) @RequestParam(name = PageRequestParameters.SIZE, required = false) @Min(PageRequestParameters.SIZE_MIN) Integer size,
                                        @ApiParam(value = PageRequestParameters.SORT_DESCRIPTION) @RequestParam(name = PageRequestParameters.SORT, required = false) String sort,
                                        @ApiParam(value = "Policy id") @RequestParam(required = false) String policyId,
                                        @ApiParam(value = "Policy type") @RequestParam(required = false) PolicyType type,
                                        @ApiParam(value = "Policy is enabled") @RequestParam(required = false) Boolean enabled) {
    PageRequestBuilder pageRequestBuilder = new PageRequestParametersReader().readParameters(page, size, sort);
    EntitySpecificationsBuilder<PDPPolicy> spec = new EntitySpecificationsBuilder<>();
    spec.with(PDPPolicy.FIELD_POLICY_ID, policyId);
    spec.with(PDPPolicy.FIELD_POLICY_TYPE, type);
    spec.with(PDPPolicy.FIELD_ENABLED, enabled);
    return pdpService.searchPolicies(spec.build(), pageRequestBuilder.build());
  }

  // We use the allowableValues specially for Swagger UI. Since it requires the value to be a constant we cannot call
  // a method from PolicyCombiningAlgorithmProvider bean to retrieve available Policy Combining Algorithm ids dynamically.
  // Thus this value just needs to be in sync with the current provider instance.
  @RequestMapping(path = "/setPolicyCombiningAlgorithm", method = RequestMethod.POST)
  public PrimitiveResult<String> setPolicyCombiningAlgorithm(@ApiParam(value = "PDP policy combining algorithm", allowableValues = DenyOverridesPolicyAlg.algId + "," +
      PermitOverridesPolicyAlg.algId + "," + FirstApplicablePolicyAlg.algId + "," + PermitUnlessDenyPolicyAlg.algId + "," + DenyUnlessPermitPolicyAlg.algId + "," +
      OrderedPermitOverridesPolicyAlg.algId + "," + OrderedDenyOverridesPolicyAlg.algId + "," + OnlyOneApplicablePolicyAlg.algId)
      @RequestParam String algorithm) throws PDPPolicyCombiningAlgorithmNotSupportedException {

    PDPConfig pdpConfig = pdpService.setPolicyCombiningAlgorithm(algorithm);
    return new PrimitiveResult<>(pdpConfig.getPolicyCombiningAlgorithm());
  }

  @RequestMapping(path = "/getPolicyCombiningAlgorithm", method = RequestMethod.GET)
  public PrimitiveResult<String> getPolicyCombiningAlgorithm() {
    return new PrimitiveResult<>(pdpService.getPolicyCombiningAlgorithm());
  }
}
