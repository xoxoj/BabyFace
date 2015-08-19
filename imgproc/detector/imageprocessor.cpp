#include "imageprocessor.h"

ImageProcessor::ImageProcessor()
{

}

cv::Mat ImageProcessor::crop(cv::Mat &input, const cv::Rect &roi) const
{
    //TODO Cropping code
}

bool ImageProcessor::applyMorphing() const
{
    return this->morphing;
}

void ImageProcessor::applyMorphing(bool apply)
{
    this->morphing = apply;
}

