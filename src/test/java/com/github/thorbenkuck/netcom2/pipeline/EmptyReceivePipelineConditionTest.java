package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.netcom2.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.clients.Connection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@Testing(QueuedReceivePipeline.class)
public class EmptyReceivePipelineConditionTest {

	@Test
	public void testEmptyReceivePipelineCondition() {
		// Arrange
		QueuedReceivePipeline<ReceivePipelineTestObject> pipeline = new QueuedReceivePipeline<>(ReceivePipelineTestObject.class);
		ReceivePipelineTestObject object = new ReceivePipelineTestObject();

		// Act
		pipeline.addFirstIfNotContained(testObject -> testObject.value = 1);
		ReceivePipelineCondition<ReceivePipelineTestObject> condition = pipeline.addFirstIfNotContained(testObject -> testObject.value = 1);
		condition.withRequirement(Session::isIdentified);
		condition.withRequirement((session, testObject) -> false);

		pipeline.run(mock(Connection.class), mock(Session.class), object);

		// Assert
		assertEquals(1, object.value);
	}

}