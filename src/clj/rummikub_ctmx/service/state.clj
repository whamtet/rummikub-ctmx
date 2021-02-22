(ns rummikub-ctmx.service.state
  (:require
    [clojure.set :as set]))

(def colors [:red :yellow :blue :black])
(def numbers (range 1 14))

(defn num-val [[_ num]]
  (cond num :joker 30 num))
(defn enumerate [s]
  (map-indexed list s))

(def tiles
  (concat
    (for [color colors number numbers i (range 2)]
      [color number i])
    [[:red :joker] [:black :joker]]))

(def state
  (atom
    {:pool (set tiles)
     :players {} ;; tile -> [player i j]
     :table {} ;; tile -> [x y]
     }))

(defn tiles-for-player
  ([player]
   (->> @state :players (tiles-for-player player)))
  ([player players]
   (for [[tile [name]] players :when (= name player)]
     tile)))

(defn sort-tiles [player tiles]
  (let [{:keys [red yellow blue black]} (group-by first tiles)
        red (sort-by num-val red)
        yellow (sort-by num-val yellow)
        blue (sort-by num-val blue)
        black (sort-by num-val black)
        first-row (concat red yellow)
        second-row (concat blue black)]
    (into {}
          (for [[i row] (enumerate [first-row second-row])
                [j tile] (enumerate row)]
            [tile [player i j]]))))

(defn sort-player [state player]
  (->> state
       :players
       (tiles-for-player player)
       (sort-tiles player)
       (update state :players merge)))
(defn sort-player! [player]
  (swap! state sort-player player))

(defn pick-up [{:keys [pool players table]} player]
  (assert (every? #(-> % second first (not= player)) players))
  (let [to-pick (->> pool shuffle (take 14))
        unsorted (concat to-pick (tiles-for-player player players))
        sorted (sort-tiles player unsorted)]
    {:pool (set/difference pool (set to-pick))
     :players (merge players sorted)
     :table table}))
(defn pick-up-one [{:keys [pool players table]} player]
  (let [to-pick (-> pool seq rand-nth)
        first-row (for [[tile [p i]] players :when (= [p i] [player 0])] tile)]
    {:pool (disj pool to-pick)
     :players (assoc players to-pick [player 0 (-> first-row count inc)])
     :table table}))

(defn pick-up-new! [player]
  (swap! state pick-up player))
(defn pick-up-one! [player]
  (swap! state pick-up-one player))

(defn put-down [{:keys [pool players table]} tile x y]
  {:pool pool
   :players (dissoc players tile)
   :table (assoc table tile [x y])})
(defn put-down! [tile x y]
  (swap! state put-down tile x y))

(defn insert-at [s i tile]
  (let [[before after] (split-at i s)]
    (concat before [tile] after)))
(defn pick-up-used [{:keys [pool players table]} tile player i j]
  (let [insert-row (for [[tile [p id]] players :when (= [p id] [player i])] tile)
        new-row (insert-at insert-row j tile)
        new-players (into {}
                          (for [[j tile] (enumerate new-row)]
                            [tile [player i j]]))]
    {:pool pool
     :players (merge players new-players)
     :table (dissoc table tile)}))
(defn pick-up-used! [tile player i j]
  (swap! state pick-up-used tile player i j))

(defn quit [{:keys [pool players table]} player]
  (let [to-remove (tiles-for-player player players)]
    {:pool (set/union pool (set to-remove))
     :players (apply dissoc players to-remove)
     :table table}))
(defn quit! [player]
  (swap! state quit player))
