package org.synbiohub.frontend;

/**
 * Thrown when the SynBioHub returns an error code.
 * @author James McLaughlin
 * @author Chris Myers
 *
 */
public class SynBioHubException extends Exception
{
      static final long serialVersionUID = 1;
    
      SynBioHubException()
      {
          super();
      }
      
      SynBioHubException(String message)
      {
          super(message);
      }
      
      SynBioHubException(String message, Throwable cause)
      {
          super(message, cause);
      }
      
      SynBioHubException(Throwable cause)
      {
          super(cause);
      }
}
