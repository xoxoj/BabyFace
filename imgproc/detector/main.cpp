#include <iostream>
#include <string>

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

#include <boost/program_options.hpp>

#include "facedetector.h"

//Cut down characters
namespace po = boost::program_options;

enum RETURN_VALUES {
    SUCCESS = 0,
    COMMAND_LINE_ERROR = 1,
    OPENCV_ERROR = 2
};

int main(int argc, char *argv[])
{
    std::string cascadeFile = "./haarcascade_frontalface_default.xml";
    FaceDetector detector(cascadeFile);

    // Declare the supported options.
    po::options_description desc("Options");
    desc.add_options()
        ("help", "I'm here to help you")
        ("detect,d", po::value<std::string>(), "Input image for face detection")
    ;

    po::variables_map vm;

    try {
        po::store(po::parse_command_line(argc, argv, desc), vm);
        po::notify(vm);

        if (vm.count("help")) {
            std::cout << desc << std::endl;
            return 1;
        }

        if (vm.count("detect")) {
            std::cout << "Input image for face detection: "
         << vm["detect"].as<std::string>() << "." << std::endl;

            std::string inputFile = vm["detect"].as<std::string>();

            try {
                cv::Mat inputImage = cv::imread(inputFile, CV_LOAD_IMAGE_UNCHANGED);

                cv::Rect roi = detector.detect(inputImage);

                cv::rectangle(inputImage, roi, cv::Scalar(0, 0, 255), 2);

                cv::imwrite("test_ouput.jpg", inputImage);

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

