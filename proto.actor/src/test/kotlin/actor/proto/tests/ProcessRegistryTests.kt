package proto.tests

import actor.proto.DeadLetterProcess
import actor.proto.PID
import actor.proto.Process
import actor.proto.ProcessRegistry
import actor.proto.fixture.TestProcess
import org.junit.Assert
import java.util.*

open class ProcessRegistryTests {
    fun given_PIDDoesNotExist_addShouldAddLocalPID() {
        val id: String = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        val pid = reg.add(id, p)
        Assert.assertEquals(reg.address, pid.address)
    }

    fun given_PIDExists_addShouldNotAddLocalPID() {
        val id: String = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        reg.add(id, p)
        val pid = reg.add(id, p)
        //throw
    }

    fun given_PIDExists_GetShouldReturnIt() {
        val id: String = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        reg.add(id, p)
        val pid = reg.add(id, p)
        val p2: Process = reg.get(pid)
        Assert.assertSame(p, p2)
    }

    fun given_PIDWasRemoved_GetShouldReturnDeadLetterProcess() {
        val id: String = UUID.randomUUID().toString()
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        val pid = reg.add(id, p)
        reg.remove(pid)
        val p2: Process = reg.get(pid)
        Assert.assertSame(DeadLetterProcess, p2)
    }

    fun given_PIDExistsInHostResolver_GetShouldReturnIt() {
        val pid: PID = PID("abc", "def")
        val p: TestProcess = TestProcess()
        val reg: ProcessRegistry = ProcessRegistry
        reg.registerHostResolver { _ -> p }
        val p2: Process = reg.get(pid)
        Assert.assertSame(p, p2)
    }
}
