import { HttpHeaders } from '@angular/common/http';
import { JWToken } from './jwt';
import { SessionData } from './sessionData';

export const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'plain/text' })
    .set('sessID', sessionStorage.getItem('session') != null ? JSON.parse(sessionStorage.getItem('session')).id : 0)
    .set('sessCSRF', sessionStorage.getItem('session') != null ? JSON.parse(sessionStorage.getItem('session')).csrf : '')
};
  
export class jwtPacker {
  public getHttpOptionsJWT(storageKey: string): HttpHeaders{
    let token: JWToken = new JWToken();
    let sessionData: SessionData = <SessionData>JSON.parse(sessionStorage.getItem(storageKey));
    
    token.payload = { 
      "iss": sessionData.Issuer,
      "sub": sessionData.Subject,
      "exp": sessionData.Expiration,
      "nbf": sessionData.notBefore,
      "iat": sessionData.IssuedAt,
      "jti": sessionData.TrackingId
    };

    return new HttpHeaders({ 'Content-Type': 'application/json', 'Authorization': 'Bearer<' + token.pack(sessionData.Secret) + '>', 'Identity': sessionData.Subject.toString()});
  }

  public getHttpOptions() : {headers}{
    return {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' })
    }
  }
}