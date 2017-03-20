package com.github.afide.model

import com.github.jtmsp.merkletree.byteable.ByteableLong
import com.github.jtmsp.merkletree.byteable.ByteablePair
import com.github.jtmsp.merkletree.byteable.ByteableString
import com.github.jtmsp.merkletree.byteable.IByteable
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

/**
 * System under specification: {@link SimpleModel}.
 * @author tglaeser
 */
class SimpleModelTest extends Specification {

    @Shared SimpleModel[] trees = [
            new SimpleModel<ByteableLong>(false){},
            new SimpleModel<ByteableString>(false){},
            new SimpleModel<ByteablePair>(false){},
    ]
    /* Note that the last entry from each list updates a previously added tx */
    @Shared IByteable[][] txss = [
            [new ByteableLong(0), new ByteableLong(1), new ByteableLong(2), new ByteableLong(3), new ByteableLong(4), new ByteableLong(5), new ByteableLong(1)],
            [new ByteableString("zero"), new ByteableString("one"), new ByteableString("two"), new ByteableString("three"), new ByteableString("four"), new ByteableString("five"), new ByteableString("three")],
            [new ByteablePair("abc"), new ByteablePair("def"), new ByteablePair("ghi=foo"), new ByteablePair("jkl=bar"), new ByteablePair("mno=baz"), new ByteablePair("xyz=qux"), new ByteablePair("def=foo")],
    ]

    @Unroll def 'test building a Merkle tree by node type'(SimpleModel tree, IByteable[] txs) {
        given: 'the Merkle tree was properly initialized'
        tree != null

        when: 'a new item is added'
        tree.deliver(txs[0].toByteArray())

        then: 'no update is expected, instead a new leaf node has been created'
        !tree.updated; tree.tree.height == 0; tree.tree.size() == 1

        when: 'a new item is added'
        tree.deliver(txs[1].toByteArray())

        then: 'no update is expected, instead a new leaf node has been created'
        !tree.updated; tree.tree.height == 1; tree.tree.size() == 2

        when: 'a new item is added'
        tree.deliver(txs[2].toByteArray())

        then: 'no update is expected, instead a new leaf node has been created'
        !tree.updated; tree.tree.height == 2; tree.tree.size() == 3

        when: 'a new item is added'
        tree.deliver(txs[3].toByteArray())

        then: 'no update is expected, instead a new leaf node has been created'
        !tree.updated; tree.tree.height == 2; tree.tree.size() == 4

        when: 'a new item is added'
        tree.deliver(txs[4].toByteArray())

        then: 'no update is expected, instead a new leaf node has been created'
        !tree.updated; tree.tree.height == 3; tree.tree.size() == 5

        when: 'a new item is added'
        tree.deliver(txs[5].toByteArray())

        then: 'no update is expected, instead a new leaf node has been created'
        !tree.updated; tree.tree.height == 3; tree.tree.size() == 6

        when: 'an existing item is updated'
        tree.deliver(txs[6].toByteArray())

        then: 'an update is expected'
        tree.updated; tree.tree.height == 3; tree.tree.size() == 6

        cleanup: 'just print the tree'
        tree.tree.iterateNodes { node ->
            if (node.isLeafNode()) {
                System.out.println(node.getKey().toPrettyString());
            } else {
                System.out.println("tree node");
            }
            return false;
        }

        where: 'the txs are of the supported differnt types'
        tree << trees
        txs << txss
    }

    def 'test persistence mode'() {
        given: 'the Merkle tree was properly initialized'
        SimpleModel tree = new SimpleModel(false)

        when: 'persistence mode is changed'
        tree.persist = true

        then: //todo
        tree.persist
    }
}