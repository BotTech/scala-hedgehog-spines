package com.lightbend.hedgehog.generators.akka.http

import akka.http.scaladsl.model
import akka.http.scaladsl.model.MediaType
import com.lightbend.hedgehog.generators.Fields
import hedgehog.Gen

object MediaTypeGenerators {

  private val MediaTypes: List[MediaType] =
    Fields.fieldsOf(model.MediaTypes, classOf[MediaType])

  private val BinaryMediaTypes: List[MediaType.Binary] =
    Fields.fieldsOf(model.MediaTypes, classOf[MediaType.Binary])

  private val MultipartMediaTypes: List[MediaType.Multipart] =
    Fields.fieldsOf(model.MediaTypes, classOf[MediaType.Multipart])

  private val NonBinaryMediaTypes: List[MediaType.NonBinary] =
    Fields.fieldsOf(model.MediaTypes, classOf[MediaType.NonBinary])

  private val WithFixedCharsetMediaTypes: List[MediaType.WithFixedCharset] =
    Fields.fieldsOf(model.MediaTypes, classOf[MediaType.WithFixedCharset])

  private val WithOpenCharsetMediaTypes: List[MediaType.WithOpenCharset] =
    Fields.fieldsOf(model.MediaTypes, classOf[MediaType.WithOpenCharset])

  private val NonMultipartWithOpenCharsetMediaTypes: List[MediaType.NonMultipartWithOpenCharset] =
    Fields.fieldsOf(model.MediaTypes, classOf[MediaType.NonMultipartWithOpenCharset])

  def genPredefinedMediaType: Gen[MediaType] =
    Gen.elementUnsafe(MediaTypes)

  def genPredefinedBinaryMediaType: Gen[MediaType.Binary] =
    Gen.elementUnsafe(BinaryMediaTypes)

  def genPredefinedMultipartMediaType: Gen[MediaType.Multipart] =
    Gen.elementUnsafe(MultipartMediaTypes)

  def genPredefinedNonBinaryMediaType: Gen[MediaType.NonBinary] =
    Gen.elementUnsafe(NonBinaryMediaTypes)

  def genPredefinedWithFixedCharsetMediaType: Gen[MediaType.WithFixedCharset] =
    Gen.elementUnsafe(WithFixedCharsetMediaTypes)

  def genPredefinedWithOpenCharsetMediaType: Gen[MediaType.WithOpenCharset] =
    Gen.elementUnsafe(WithOpenCharsetMediaTypes)

  def genPredefinedNonMultipartWithOpenCharsetMediaType: Gen[MediaType.NonMultipartWithOpenCharset] =
    Gen.elementUnsafe(NonMultipartWithOpenCharsetMediaTypes)

  // TODO: Generate other media types.

  def genMediaType: Gen[MediaType] =
    Gen.choice1(genPredefinedMediaType)

  def genBinaryMediaType: Gen[MediaType.Binary] =
    Gen.choice1(genPredefinedBinaryMediaType)

  def genMultipartMediaType: Gen[MediaType.Multipart] =
    Gen.choice1(genPredefinedMultipartMediaType)

  def genNonBinaryMediaType: Gen[MediaType.NonBinary] =
    Gen.choice1(genPredefinedNonBinaryMediaType)

  def genWithFixedCharsetMediaType: Gen[MediaType.WithFixedCharset] =
    Gen.choice1(genPredefinedWithFixedCharsetMediaType)

  def genWithOpenCharsetMediaType: Gen[MediaType.WithOpenCharset] =
    Gen.choice1(genPredefinedWithOpenCharsetMediaType)

  def genNonMultipartWithOpenCharsetMediaType: Gen[MediaType.NonMultipartWithOpenCharset] =
    Gen.choice1(genPredefinedNonMultipartWithOpenCharsetMediaType)
}
