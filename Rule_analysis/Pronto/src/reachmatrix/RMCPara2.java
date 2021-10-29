package reachmatrix;

/**
 * use atomic action, lock
 * ind = globalind
 * globalind ++
 * 
 * then do
 * if ind == .length
 * return
 * else
 * calculate(ind)
 * not use thread pool
 * maybe it is not necessary
 * @author hongkun
 *
 */

public class RMCPara2 {

}
