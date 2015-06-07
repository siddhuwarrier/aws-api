package info.siddhuw.models

/**
 * @author Siddhu Warrier
 */

/**
 * Describes an AWS region. Not using the AWS SDK classes directly in controller to decouple the AWS SDK
 * from the code as far as possible (also JSON renders more easily with case classes
 *
 * @param id region ID
 */
case class AWSRegion(id: String)
