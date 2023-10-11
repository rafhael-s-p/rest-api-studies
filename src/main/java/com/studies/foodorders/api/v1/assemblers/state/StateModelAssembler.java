package com.studies.foodorders.api.v1.assemblers.state;

import com.studies.foodorders.api.v1.controllers.localization.StateController;
import com.studies.foodorders.api.v1.links.StateLinks;
import com.studies.foodorders.api.v1.models.localization.state.StateInput;
import com.studies.foodorders.api.v1.models.localization.state.StateModel;
import com.studies.foodorders.domain.models.localization.State;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class StateModelAssembler extends RepresentationModelAssemblerSupport<State, StateModel> {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private StateLinks stateLinks;

    public StateModelAssembler() {
        super(StateController.class, StateModel.class);
    }

    public StateModel toModel(State state) {

        StateModel stateModel = createModelWithId(state.getId(), state);
        modelMapper.map(state, stateModel);

        stateModel.add(stateLinks.linkToStates("states"));

        return stateModel;
    }

    @Override
    public CollectionModel<StateModel> toCollectionModel(Iterable<? extends State> entities) {
        return super.toCollectionModel(entities)
                .add(stateLinks.linkToStates());
    }

    public State toDomainObject(StateInput stateInput) {
        return modelMapper.map(stateInput, State.class);
    }

    public void copyToDomainObject(StateInput stateInput, State state) {
        modelMapper.map(stateInput, state);
    }

}