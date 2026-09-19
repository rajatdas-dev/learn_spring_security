# learn_spring_security
Just to learn spring security

# Configuration 

I used two services of spring security and they are UserDetailsService and UserDetails 

<ul>

<li>

<b>
UserDetailsService :
</b> this is the main service which retrieves security information about users. As we defined our custom bean, we don't see anymore in the log the default password provided by Spring, as we are now providing it. 
</li>

<li>
<b>
UserDetails 
</b> class : this is the interface which Spring uses to process user's security related information, like the username or its password. We are using the Standard User class as its implementation. 
</li>
</ul>

<p> 
The password is prefixed with <i> {noop} </i> to avoid a password encoder, so that our real password is  <b> <i> ourpassword </i> </b>.  
</p>

