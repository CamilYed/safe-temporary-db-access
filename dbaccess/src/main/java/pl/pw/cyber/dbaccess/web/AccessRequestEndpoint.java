package pl.pw.cyber.dbaccess.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/access-request")
class AccessRequestEndpoint {

  @PostMapping
  public ResponseEntity<Void> requestAccess() {
    return ResponseEntity.ok().build();
  }
}
