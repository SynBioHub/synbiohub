WITH subtree AS 
( SELECT root.id, root.uri, root.privilege, root.userId
  FROM auth root
  WHERE root.id = 2319
  
  UNION ALL
  
  SELECT child.id, child.uri, child.privilege, child.userId
  FROM subtree parent, auth child
  WHERE child.rootAuth = parent.id
 )
SELECT * FROM subtree