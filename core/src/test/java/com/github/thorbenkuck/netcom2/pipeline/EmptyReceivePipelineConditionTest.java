package com.github.thorbenkuck.netcom2.pipeline;

import com.github.thorbenkuck.keller.annotations.Testing;
import com.github.thorbenkuck.netcom2.network.shared.Session;
import com.github.thorbenkuck.netcom2.network.shared.connections.ConnectionContext;
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
		condition.require(Session::isIdentified);
		condition.require((session, testObject) -> false);

		pipeline.run(mock(ConnectionContext.class), mock(Session.class), object);

		// Assert
		assertEquals(1, object.value);
	}

}