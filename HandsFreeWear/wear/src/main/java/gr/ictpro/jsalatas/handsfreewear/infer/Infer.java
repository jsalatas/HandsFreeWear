package gr.ictpro.jsalatas.handsfreewear.infer;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import gr.ictpro.jsalatas.handsfreewear.application.HandsFreeWearApplication;

/**
 * Created by john on 1/1/18.
 */

class Infer {
    private static Infer infer;

    private final Queue queue = Queue.getInstance();
    private final State state = State.getInstance();
    private final TensorFlowInferenceInterface inferenceInterface;


    private Infer() {
        inferenceInterface = new TensorFlowInferenceInterface(HandsFreeWearApplication.getAssetManager(), "file:///android_asset/frozen_model.pb");
    }

    static Infer getInstance() {
        if(infer == null) {
            infer = new Infer();
        }

        return infer;
    }

    void inferGesture() {
        float[] outputScores = new float[15];
        float[] input = queue.getRecord();
        int predicted = 0;
        if(input != null) {
            inferenceInterface.feed("input_input_1", input, 1, Queue.MAX_ELEMENTS);
            inferenceInterface.run(new String[]{"output_1/Softmax"});
            inferenceInterface.fetch("output_1/Softmax", outputScores);
            float prob = 0.f;
            for (int i = 0; i < outputScores.length; i++) {
                if (prob < outputScores[i]) {
                    predicted = i;
                    prob = outputScores[i];
                }
            }
        }

        state.toState(predicted);
    }


}
