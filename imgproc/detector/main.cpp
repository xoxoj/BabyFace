#include <iostream>
#include <string>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include <boost/program_options.hpp>

#include "facedetector.h"
#include "imageprocessor.h"

//Cut down characters
namespace po = boost::program_options;

enum RETURN_VALUES {
    SUCCESS = 0,
    COMMAND_LINE_ERROR = 1,
    OPENCV_ERROR = 2,
    DETECTION_ERROR = 3
};

int main(int argc, char *argv[])
{
    std::string cascadeFile = "./haarcascade_frontalface_default.xml";
    FaceDetector detector(cascadeFile);
    ImageProcessor processor;

    // Declare the supported options.
    po::options_description desc("Options");
    desc.add_options()
        ("help", "I'm here to help you")
        ("process,p", po::value<std::string>()->required(), "Input image")
        ("output,o", po::value<std::string>()->required(), "Output image")
        ("morph,m", po::value<bool>()->default_value(false), "Apply morphing (when its done")
        ("enhance,e", po::value<bool>()->default_value(false), "Apply automatic image enhancement (experimental)")
    ;

    po::variables_map vm;

    try {
        po::store(po::parse_command_line(argc, argv, desc), vm);
        po::notify(vm);

        if (vm.count("help")) {
            std::cout << desc << std::endl;
            return 1;
        }

        if(vm.count("morph")) {
            std::cout << "Apply image morphing: " << vm["morph"].as<bool>() << std::endl;
            processor.applyMorphing(vm["morph"].as<bool>());
        }
        if(vm.count("enhance")) {
            std::cout << "Apply image enhancement: " << vm["enhance"].as<bool>() << std::endl;
            processor.enhance(vm["enhance"].as<bool>());
        }
        if(vm.count("output")) {
            std::cout << "Output image: " << vm["output"].as<std::string>() << "." << std::endl;
        }

        if (vm.count("process")) {
            std::cout << "Input image: " << vm["process"].as<std::string>() << "." << std::endl;

            std::string inputFile = vm["process"].as<std::string>();
            std::string outputFile = vm["output"].as<std::string>();

            try {
                cv::Mat inputImage = cv::imread(inputFile, CV_LOAD_IMAGE_UNCHANGED);
                cv::Rect roi = detector.detect(inputImage);

                if(roi.area() == 0) {
                    return RETURN_VALUES::DETECTION_ERROR;
                }

                cv::rectangle(inputImage, roi, cv::Scalar(0, 0, 255), 2);

                cv::Mat newImage = processor.crop(inputImage, roi);

                if(processor.applyMorphing()) {
                    std::cout << "I'm a morphing dummy" << std::endl;
                }
                if(processor.enhance()) {
                    std::cout << "Enhancing..." << std::endl;
                    newImage = processor.equalizeHistogram(newImage);
                }

                cv::imwrite(outputFile, newImage);

                return RETURN_VALUES::SUCCESS;

//                cv::namedWindow("preview");
//                cv::imshow("preview", inputImage);
//                cv::waitKey(0);
            } catch (cv::Exception &e) {
                std::cerr << e.what();
                exit(RETURN_VALUES::OPENCV_ERROR);
            }

        } else {
            std::cerr << "No input image given." << std::endl;
            exit(RETURN_VALUES::COMMAND_LINE_ERROR);
        }
    } catch (po::error &e) {
        std::cerr << "ERROR: " << e.what() << std::endl << std::endl;
        std::cout << desc << std::endl;
        return RETURN_VALUES::COMMAND_LINE_ERROR;
    }

    return RETURN_VALUES::SUCCESS;
}

