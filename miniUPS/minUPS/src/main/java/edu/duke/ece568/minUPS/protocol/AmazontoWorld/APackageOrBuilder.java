// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: AmazontoWorld.proto

package edu.duke.ece568.minUPS.protocol.AmazontoWorld;

public interface APackageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:APackage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>required int64 packageid = 1;</code>
   * @return Whether the packageid field is set.
   */
  boolean hasPackageid();
  /**
   * <code>required int64 packageid = 1;</code>
   * @return The packageid.
   */
  long getPackageid();

  /**
   * <code>required string status = 2;</code>
   * @return Whether the status field is set.
   */
  boolean hasStatus();
  /**
   * <code>required string status = 2;</code>
   * @return The status.
   */
  java.lang.String getStatus();
  /**
   * <code>required string status = 2;</code>
   * @return The bytes for status.
   */
  com.google.protobuf.ByteString
      getStatusBytes();

  /**
   * <code>required int64 seqnum = 3;</code>
   * @return Whether the seqnum field is set.
   */
  boolean hasSeqnum();
  /**
   * <code>required int64 seqnum = 3;</code>
   * @return The seqnum.
   */
  long getSeqnum();
}
